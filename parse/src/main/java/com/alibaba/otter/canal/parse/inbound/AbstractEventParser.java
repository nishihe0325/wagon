package com.alibaba.otter.canal.parse.inbound;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.otter.canal.common.AbstractCanalLifeCycle;
import com.alibaba.otter.canal.common.alarm.CanalAlarmHandler;
import com.alibaba.otter.canal.filter.CanalEventFilter;
import com.alibaba.otter.canal.parse.CanalEventParser;
import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.exception.TableIdNotFoundException;
import com.alibaba.otter.canal.parse.inbound.EventTransactionBuffer.TransactionFlushCallback;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.index.CanalLogPositionManager;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.sink.CanalEventSink;
import com.alibaba.otter.canal.sink.exception.CanalSinkException;

/**
 * 抽象的EventParser, 最大化共用mysql/oracle版本的实现
 *
 * @author jianghang 2013-1-20 下午08:10:25
 * @version 1.0.0
 */
public abstract class AbstractEventParser<EVENT> extends AbstractCanalLifeCycle implements CanalEventParser<EVENT> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected CanalEventSink<List<CanalEntry.Entry>> eventSink = null;
    protected CanalEventFilter eventFilter = null;
    protected CanalEventFilter eventBlackFilter = null;
    protected CanalLogPositionManager logPositionManager = null;
    private CanalAlarmHandler alarmHandler = null;

    // 统计参数
    protected AtomicBoolean profilingEnabled = new AtomicBoolean(false);
    protected AtomicLong receivedEventCount = new AtomicLong();
    protected AtomicLong parsedEventCount = new AtomicLong();
    protected AtomicLong consumedEventCount = new AtomicLong();
    protected long parsingInterval = -1;
    protected long processingInterval = -1;

    // 认证信息
    protected volatile AuthenticationInfo runningInfo;
    protected String destination;

    // binLogParser
    protected BinlogParser binlogParser = null;

    protected Thread parseThread = null;
    protected Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("parse events has an error", e);
        }
    };

    protected EventTransactionBuffer transactionBuffer;
    protected int transactionSize = 1024;
    protected AtomicBoolean needTransactionPosition = new AtomicBoolean(false);
    protected long lastEntryTime = 0L;
    protected long lastDumpTime = System.currentTimeMillis();
    protected volatile boolean detectingEnable = true; // 是否开启心跳检查
    protected Integer detectingIntervalInSeconds = 3; // 检测频率

    protected volatile Timer timer;
    protected TimerTask heartBeatTimerTask;
    protected Throwable exception = null;

    protected BinlogSequenceNo binlogSequenceNo = new BinlogSequenceNo();
    protected BinlogSequenceNo transactionNoGenerator = new BinlogSequenceNo();
    protected long transactionNo = transactionNoGenerator.getBinlogSequenceNo();

    protected abstract BinlogParser buildParser();

    protected abstract ErosaConnection buildErosaConnection();

    protected abstract EntryPosition findStartPosition(ErosaConnection connection) throws IOException;

    protected void preDump(ErosaConnection connection) {
    }

    protected void afterDump(ErosaConnection connection) {
    }

    public void sendAlarm(String destination, String msg) {
        if (this.alarmHandler != null) {
            this.alarmHandler.sendAlarm(destination, msg);
        }
    }

    public AbstractEventParser() {
        transactionBuffer = new EventTransactionBuffer(new TransactionFlushCallback() {
            public void flush(List<CanalEntry.Entry> transaction) throws InterruptedException {
                boolean successed = consumeTheEventAndProfilingIfNecessary(transaction);
                if (!running) {
                    return;
                }

                if (!successed) {
                    throw new CanalParseException("consume failed!");
                }

                LogPosition position = buildLastTransactionPosition(transaction);
                if (position != null) { // 可能position为空
                    logPositionManager.persistLogPosition(AbstractEventParser.this.destination, position);
                }
            }
        });
    }

    public void start() {
        super.start();
        MDC.put("destination", destination);

        // 配置transaction buffer,初始化缓冲队列,并启动
        transactionBuffer.setBufferSize(transactionSize);// 设置buffer大小
        transactionBuffer.start();

        // 构造binlog parser,并启动
        binlogParser = buildParser();
        binlogParser.start();

        // 新建并启动工作线程
        parseThread = new Thread(new Runnable() {
            public void run() {
                MDC.put("destination", String.valueOf(destination));
                ErosaConnection erosaConnection = null;

                while (running) {
                    try {
                        // 开始执行replication
                        erosaConnection = buildErosaConnection(); // 1. 构造Erosa连接
                        startHeartBeat(erosaConnection);          // 2. 启动一个心跳线程
                        preDump(erosaConnection);                 // 3. 执行dump前的准备工作
                        erosaConnection.connect();                // 4. 链接
                        final EntryPosition startPosition = findStartPosition(erosaConnection); // 5. 获取最后的位置信息
                        if (startPosition == null) {
                            throw new CanalParseException("can't find start position for " + destination);
                        }
                        logger.info("find start position : {}", startPosition.toString());
                        erosaConnection.reconnect();             // 6. 重新链接，因为在找position过程中可能有状态，需要断开后重建

                        // 7. 初始化sink处理方法
                        final SinkFunction sinkHandler = new SinkFunction<EVENT>() {
                            private LogPosition lastPosition;

                            public boolean sink(EVENT event) {
                                try {
                                    lastDumpTime = System.currentTimeMillis();
                                    CanalEntry.Entry entry = parseAndProfilingIfNecessary(event);
                                    if (!running) {
                                        return false;
                                    }
                                    if (entry != null) {
                                        entry.getHeader().setBinlogSequenceNo(binlogSequenceNo.getBinlogSequenceNo()); // 增加序号,用以确定两条消息的顺序
                                        if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN) {
                                            transactionNo = transactionNoGenerator.getBinlogSequenceNo(); // 事务开始,重新生成事务号👌
                                        }
                                        entry.getHeader().setTransactionNo(transactionNo); // 增加事务号👌

                                        exception = null; // 有正常数据流过，清空exception
                                        transactionBuffer.add(entry);
                                        this.lastPosition = buildLastPosition(entry); // 记录一下对应的positions
                                        lastEntryTime = System.currentTimeMillis(); // 记录一下最后一次有数据的时间
                                    }
                                    return running;
                                } catch (TableIdNotFoundException e) {
                                    throw e;
                                } catch (Exception e) { // 记录一下，出错的位点信息
                                    processError(e, this.lastPosition, startPosition.getJournalName(), startPosition.getPosition());
                                    throw new CanalParseException(e); // 继续抛出异常，让上层统一感知
                                }
                            }
                        };

                        // 8. 开始dump数据
                        if (StringUtils.isEmpty(startPosition.getJournalName()) && startPosition.getTimestamp() != null) {
                            erosaConnection.dump(startPosition.getTimestamp(), sinkHandler);
                        } else {
                            erosaConnection.dump(startPosition.getJournalName(), startPosition.getPosition(), sinkHandler);
                        }
                    } catch (TableIdNotFoundException e) {
                        // 特殊处理TableIdNotFound异常,出现这样的异常，一种可能就是起始的position是一个事务当中，导致tablemap Event时间没解析过
                        exception = e;
                        needTransactionPosition.compareAndSet(false, true);
                        logger.error(String.format("dump address %s has an error, retrying. caused by ", runningInfo.getAddress().toString()), e);
                    } catch (Throwable e) {
                        exception = e;
                        if (!running) {
                            if (!(e instanceof java.nio.channels.ClosedByInterruptException || e.getCause() instanceof java.nio.channels.ClosedByInterruptException)) {
                                throw new CanalParseException(String.format("dump address %s has an error, retrying. ", runningInfo.getAddress().toString()), e);
                            }
                        } else {
                            logger.error(String.format("dump address %s has an error, retrying. caused by ", runningInfo.getAddress().toString()), e);
                            sendAlarm(destination, ExceptionUtils.getFullStackTrace(e));
                        }
                    } finally {
                        // 关闭一下链接
                        afterDump(erosaConnection);
                        try {
                            if (erosaConnection != null) {
                                erosaConnection.disconnect();
                            }
                        } catch (IOException e1) {
                            if (!running) {
                                throw new CanalParseException(String.format("disconnect address %s has an error, retrying. ", runningInfo.getAddress().toString()), e1);
                            } else {
                                logger.error("disconnect address {} has an error, retrying., caused by ", runningInfo.getAddress().toString(), e1);
                            }
                        }
                    }

                    // 出异常了，退出sink消费，释放一下状态
                    eventSink.interrupt();
                    transactionBuffer.reset();// 重置一下缓冲队列，重新记录数据
                    binlogParser.reset();// 重新置位

                    if (running) {
                        try {
                            Thread.sleep(10000 + RandomUtils.nextInt(10000)); // sleep一段时间再进行重试
                        } catch (InterruptedException e) {
                        }
                    }
                }

                MDC.remove("destination");
            }
        });
        parseThread.setUncaughtExceptionHandler(handler);
        parseThread.setName(String.format("destination = %s , address = %s , EventParser", destination, runningInfo == null ? null : runningInfo.getAddress().toString()));
        parseThread.start();
    }

    public void stop() {
        super.stop();

        stopHeartBeat(); // 先停止心跳
        parseThread.interrupt(); // 尝试中断
        eventSink.interrupt();
        try {
            parseThread.join();// 等待其结束
        } catch (InterruptedException e) {
            // ignore
        }

        if (binlogParser.isStart()) {
            binlogParser.stop();
        }
        if (transactionBuffer.isStart()) {
            transactionBuffer.stop();
        }
    }

    protected boolean consumeTheEventAndProfilingIfNecessary(List<CanalEntry.Entry> entrys) throws CanalSinkException, InterruptedException {
        long startTs = -1;
        boolean enabled = getProfilingEnabled();
        if (enabled) {
            startTs = System.currentTimeMillis();
        }

        boolean result = eventSink.sink(entrys, (runningInfo == null) ? null : runningInfo.getAddress(), destination);

        if (enabled) {
            this.processingInterval = System.currentTimeMillis() - startTs;
        }

        if (consumedEventCount.incrementAndGet() < 0) {
            consumedEventCount.set(0);
        }

        return result;
    }

    protected CanalEntry.Entry parseAndProfilingIfNecessary(EVENT bod) throws Exception {
        long startTs = -1;
        boolean enabled = getProfilingEnabled();
        if (enabled) {
            startTs = System.currentTimeMillis();
        }
        CanalEntry.Entry event = binlogParser.parse(bod);

        if (enabled) {
            this.parsingInterval = System.currentTimeMillis() - startTs;
        }

        if (parsedEventCount.incrementAndGet() < 0) {
            parsedEventCount.set(0);
        }
        return event;
    }

    public Boolean getProfilingEnabled() {
        return profilingEnabled.get();
    }

    protected LogPosition buildLastTransactionPosition(List<CanalEntry.Entry> entries) { // 初始化一下
        for (int i = entries.size() - 1; i > 0; i--) {
            CanalEntry.Entry entry = entries.get(i);
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {// 尽量记录一个事务做为position
                return buildLastPosition(entry);
            }
        }

        return null;
    }

    protected LogPosition buildLastPosition(CanalEntry.Entry entry) { // 初始化一下
        LogPosition logPosition = new LogPosition();
        EntryPosition position = new EntryPosition();
        position.setJournalName(entry.getHeader().getLogfileName());
        position.setPosition(entry.getHeader().getLogfileOffset());
        position.setTimestamp(entry.getHeader().getExecuteTime());
        logPosition.setPostion(position);

        LogIdentity identity = new LogIdentity(runningInfo.getAddress(), -1L);
        logPosition.setIdentity(identity);
        return logPosition;
    }

    protected void processError(Exception e, LogPosition lastPosition, String startBinlogFile, long startPosition) {
        if (lastPosition != null) {
            logger.warn(String.format("ERROR ## parse this event has an error , last position : [%s]", lastPosition.getPostion()), e);
        } else {
            logger.warn(String.format("ERROR ## parse this event has an error , last position : [%s,%s]", startBinlogFile, startPosition), e);
        }
    }

    protected void startHeartBeat(ErosaConnection connection) {
        lastEntryTime = 0L; // 初始化
        if (timer == null) {// lazy初始化一下
            String name = String.format("destination = %s , address = %s , HeartBeatTimeTask", destination, runningInfo == null ? null : runningInfo.getAddress().toString());
            synchronized (MysqlEventParser.class) {
                if (timer == null) {
                    timer = new Timer(name, true);
                }
            }
        }

        if (heartBeatTimerTask == null) {// fixed issue #56，避免重复创建heartbeat线程
            heartBeatTimerTask = buildHeartBeatTimeTask(connection);
            Integer interval = detectingIntervalInSeconds;
            timer.schedule(heartBeatTimerTask, interval * 1000L, interval * 1000L);
            logger.info("start heart beat.... ");
        }
    }

    protected TimerTask buildHeartBeatTimeTask(ErosaConnection connection) {
        return new TimerTask() {
            public void run() {
                try {
                    if (exception == null || lastEntryTime > 0) {
                        // 如果未出现异常，或者有第一条正常数据
                        long now = System.currentTimeMillis();
                        long inteval = (now - lastEntryTime) / 1000;
                        if (inteval >= detectingIntervalInSeconds) {
                            Header.Builder headerBuilder = Header.newBuilder();
                            headerBuilder.setExecuteTime(now);
                            Entry.Builder entryBuilder = Entry.newBuilder();
                            entryBuilder.setHeader(headerBuilder.build());
                            entryBuilder.setEntryType(EntryType.HEARTBEAT);
                            Entry entry = entryBuilder.build();
                            // 提交到sink中，目前不会提交到store中，会在sink中进行忽略
                            consumeTheEventAndProfilingIfNecessary(Arrays.asList(entry));
                        }
                    }
                } catch (Throwable e) {
                    logger.warn("heartBeat run failed " + ExceptionUtils.getStackTrace(e));
                }
            }
        };
    }

    protected void stopHeartBeat() {
        lastEntryTime = 0L; // 初始化
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        heartBeatTimerTask = null;
    }

    // ======================== static class ================================================

    // 该类对象被单线程访问,因此不会出现并发问题
    private static class BinlogSequenceNo {
        private long systemTimestamp;
        private long systemTimestampPrefix;
        private long counter;

        public BinlogSequenceNo() {
            systemTimestamp = System.currentTimeMillis();
            systemTimestampPrefix = systemTimestamp * 100 * 10000;
            counter = 1;
        }

        public long getBinlogSequenceNo() {
            // 如果当前时间跨度超过最大时间跨度(1秒),或当前时间跨度内的数据量达到最大值(99999条),则重新生成时间前缀和计数器
            if (System.currentTimeMillis() - systemTimestamp > 1000 || counter >= 1000000) {
                // 如果当前时间比时间戳前缀还小或相等(这种可能性很小,即只满足上述条件的counter >= 1000000条件,
                // 也说明,上一毫秒内发送消息超过1000000条),
                // 则休眠一毫秒,然后重试,直到成功(即,当前时间比时间戳前缀大,严格保证不同跨度的时间戳前缀是递增的)
                while (true) {
                    long curTimestamp = System.currentTimeMillis();
                    if (curTimestamp > this.systemTimestamp) {
                        // 当前时间比时间戳前缀大,则重新设置前缀值,且计数器复位,并跳出循环
                        systemTimestamp = curTimestamp;
                        systemTimestampPrefix = systemTimestamp * 100 * 10000;
                        counter = 1;
                        break;
                    }

                    // 休眠一毫秒后重试
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }

            // 拼接序列号: 后六位之前的值为时间戳(不是当前时间戳,最大以1秒为一个跨度范围),后六位是该时间范围内的递增计数器值
            return systemTimestampPrefix + counter++;
        }
    }

    // ======================== get and set ================================================
    public void setEventFilter(CanalEventFilter eventFilter) {
        this.eventFilter = eventFilter;
    }

    public void setEventBlackFilter(CanalEventFilter eventBlackFilter) {
        this.eventBlackFilter = eventBlackFilter;
    }

    public Long getParsedEventCount() {
        return parsedEventCount.get();
    }

    public Long getConsumedEventCount() {
        return consumedEventCount.get();
    }

    public void setProfilingEnabled(boolean profilingEnabled) {
        this.profilingEnabled = new AtomicBoolean(profilingEnabled);
    }

    public long getParsingInterval() {
        return parsingInterval;
    }

    public long getProcessingInterval() {
        return processingInterval;
    }

    public void setEventSink(CanalEventSink<List<CanalEntry.Entry>> eventSink) {
        this.eventSink = eventSink;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setBinlogParser(BinlogParser binlogParser) {
        this.binlogParser = binlogParser;
    }

    public BinlogParser getBinlogParser() {
        return binlogParser;
    }

    public void setAlarmHandler(CanalAlarmHandler alarmHandler) {
        this.alarmHandler = alarmHandler;
    }

    public CanalAlarmHandler getAlarmHandler() {
        return this.alarmHandler;
    }

    public void setLogPositionManager(CanalLogPositionManager logPositionManager) {
        this.logPositionManager = logPositionManager;
    }

    public void setTransactionSize(int transactionSize) {
        this.transactionSize = transactionSize;
    }

    public CanalLogPositionManager getLogPositionManager() {
        return logPositionManager;
    }

    public void setDetectingEnable(boolean detectingEnable) {
        this.detectingEnable = detectingEnable;
    }

    public void setDetectingIntervalInSeconds(Integer detectingIntervalInSeconds) {
        this.detectingIntervalInSeconds = detectingIntervalInSeconds;
    }

    public Throwable getException() {
        return exception;
    }

    public AuthenticationInfo getRunningInfo() {
        return runningInfo;
    }

    public long getLastDumpTime() {
        return lastDumpTime;
    }

}
