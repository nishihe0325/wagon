package com.youzan.wagon.binlog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.inbound.SinkFunction;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection;
import com.alibaba.otter.canal.parse.inbound.mysql.dbsync.LogEventConvert;
import com.alibaba.otter.canal.parse.inbound.mysql.dbsync.TableMetaCache;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.taobao.tddl.dbsync.binlog.LogEvent;
import com.youzan.wagon.binlog.config.ConnectionConfig;
import com.youzan.wagon.binlog.handler.EntryHandler;

public class MysqlBinlogParser implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(MysqlBinlogParser.class);
    private final static Logger DEBUG_LOG = LoggerFactory.getLogger("DEBUG");
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 连接信息
    private String host;
    private Integer port;
    private String username;
    private String password;

    // 查找范围
    private EntryPosition startPosition;
    private EntryPosition endPosition;
    private Long startTimestamp;
    private Long endTimestamp;

    // Entry处理器
    private EntryHandler entryHandler;
    private CountDownLatch countDownLatch;

    // 解析信息
    private MysqlConnection metaConnection;
    private LogEventConvert binlogParser;

    // ============================================================

    public MysqlBinlogParser(String host, Integer port, String username, String password, CountDownLatch countDownLatch, EntryHandler entryHandler) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.countDownLatch = countDownLatch;
        this.entryHandler = entryHandler;
    }

    public MysqlBinlogParser(ConnectionConfig config, CountDownLatch countDownLatch, EntryHandler entryHandler) {
        this(config.getIp(), config.getPort(), config.getUsername(), config.getPassword(), countDownLatch, entryHandler);

        String[] array = config.getStartPosition().split(":");
        startPosition = new EntryPosition(array[0], Long.valueOf(array[1]));
        array = config.getEndPosition().split(":");
        endPosition = new EntryPosition(array[0], Long.valueOf(array[1]));
    }

    public void run() {
        checkPosition(); // 检测参数设置

        // 启动binlogParser
        binlogParser = buildParser();
        binlogParser.start();
        MysqlConnection connection = null;

        try {
            // 建立连接
            connection = MysqlBinlogHelper.buildConnection(host, port, username, password);
            preDump(connection);
            connection.connect();

            // 查找开始位点
            EntryPosition dumpPosition = findStartPosition(connection);
            resetEndPosition(connection); // 重新设置结束位点

            // 开始dump
            InetSocketAddress remoteAddr = connection.getConnector().getAddress();
            SinkFunction<LogEvent> sinkHandler = new DefaultSinkFunction(remoteAddr);
            LOG.info("start dump from: {}, {}:{}", remoteAddr, dumpPosition.getJournalName(), dumpPosition.getPosition());
            connection.dump(dumpPosition.getJournalName(), dumpPosition.getPosition(), sinkHandler);
        } catch (Throwable e) {
            LOG.error(String.format("dump from %s:%d error, caused by: %s", host, port, ExceptionUtils.getFullStackTrace(e)));
        } finally {
            afterDump(connection);
            countDownLatch.countDown();
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder("[MysqlBinlogParser:");

        buffer.append("ip=").append(host);
        buffer.append(";port=").append(port);
        buffer.append(";username=").append(username);
        buffer.append(";password=").append(password);

        buffer.append(";startTimestamp=").append(startTimestamp);
        buffer.append(";endTimestamp=").append(endTimestamp);

        if (startPosition == null) {
            buffer.append(";startPosition=null");
        } else {
            buffer.append(";startPosition=").append(startPosition.getJournalName()).append(":").append(startPosition.getPosition());
        }

        if (endPosition == null) {
            buffer.append(";endPosition=null");
        } else {
            buffer.append(";endPosition=").append(endPosition.getJournalName()).append(":").append(endPosition.getPosition());
        }

        buffer.append("]");
        return buffer.toString();
    }

    // =================== helper method ================================

    private void checkPosition() {
        // startTimestamp和startPosition不能同时为空，或同时不为空
        if (startTimestamp == null //
                && (startPosition == null || StringUtils.isBlank(startPosition.getJournalName()) || startPosition.getPosition() == null)) {
            throw new CanalParseException("All the startTimestamp and startPosition are blank.");
        }
        if (startTimestamp != null && startPosition != null) {
            throw new CanalParseException("Can't set startTimestamp and startPosition the same time.");
        }

        // endTimestamp和endPosition不能同时不为空
        if (endTimestamp != null && endPosition != null) {
            throw new CanalParseException("All the endTimestamp and endPosition are blank.");
        }
    }

    private LogEventConvert buildParser() {
        LogEventConvert convert = new LogEventConvert();
        convert.setCharset(Charset.forName("UTF-8"));
        convert.setFilterQueryDcl(false);
        convert.setFilterQueryDml(false);
        convert.setFilterQueryDdl(false);
        convert.setFilterTableError(false);
        return convert;
    }

    private void preDump(MysqlConnection connection) {
        metaConnection = connection.fork();

        try {
            metaConnection.connect();
        } catch (Exception e) {
            throw new CanalParseException(e);
        }

        TableMetaCache tableMetaCache = new TableMetaCache(metaConnection);
        binlogParser.setTableMetaCache(tableMetaCache);
    }

    private void afterDump(MysqlConnection connection) {
        if (metaConnection != null) {
            try {
                metaConnection.disconnect();
            } catch (IOException e) {
                LOG.error("disconnect meta connection for address:{}", metaConnection.getConnector().getAddress(), e);
            }
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (IOException e1) {
                LOG.error("disconnect connection for address:{}", connection.getConnector().getAddress(), e1);
            }
        }
    }

    private void resetEndPosition(MysqlConnection connection) {
        // endTimestamp和endPosition都未空
        if (endTimestamp == null //
                && (endPosition == null || StringUtils.isBlank(endPosition.getJournalName()) || endPosition.getPosition() == null)) {
            endPosition = findEndPosition(connection);
        }
    }

    private EntryPosition findStartPosition(MysqlConnection connection) throws IOException {
        // 重新查找开始位点
        if (startPosition != null && StringUtils.isNotBlank(startPosition.getJournalName()) && startPosition.getPosition() != null) {
            return startPosition;
        } else {
            EntryPosition dumpPosition = findByStartTimeStamp(connection, startTimestamp);
            if (dumpPosition == null) {
                throw new CanalParseException(String.format("cant find position for startTimestamp %d", startTimestamp));
            }
            return dumpPosition;
        }
    }

    private EntryPosition findByStartTimeStamp(MysqlConnection connection, Long startTimestamp) throws IOException {
        EntryPosition startPosition = findFirstPosition(connection);
        EntryPosition endPosition = findEndPosition(connection);
        String minBinlogFileName = startPosition.getJournalName();
        String startSearchBinlogFile = endPosition.getJournalName();
        boolean shouldBreak = false;

        while (!shouldBreak) {
            // 获取该文件的第一个position
            EntryPosition firstPosition = findFirstPositionInSpecificLogFile(connection, startSearchBinlogFile);
            if (firstPosition == null) {
                throw new CanalParseException(String.format("the firstPosition is null for %s, %s", connection.getConnector().getAddress(), startSearchBinlogFile));
            }

            // 如果该position的执行时间小于等于startTimestamp
            if (firstPosition.getTimestamp() <= startTimestamp) {
                return firstPosition;
            }

            // 已经找到最早的一个binlog，没必要往前找了
            if (StringUtils.equalsIgnoreCase(minBinlogFileName, startSearchBinlogFile)) {
                return null;
            }

            // 继续往前找
            int binlogSeqNum = Integer.parseInt(startSearchBinlogFile.substring(startSearchBinlogFile.indexOf(".") + 1));
            if (binlogSeqNum <= 1) {
                shouldBreak = true; // 跳出
            } else {
                int nextBinlogSeqNum = binlogSeqNum - 1;
                String binlogFileNamePrefix = startSearchBinlogFile.substring(0, startSearchBinlogFile.indexOf(".") + 1);
                String binlogFileNameSuffix = String.format("%06d", nextBinlogSeqNum);
                startSearchBinlogFile = binlogFileNamePrefix + binlogFileNameSuffix;
            }
        }

        // 找不到
        return null;
    }

    /**
     * 查找指定binlog文件的第一个position
     * 
     * @param connection
     * @param binlogfilename
     * @return
     * @throws IOException
     */
    private EntryPosition findFirstPositionInSpecificLogFile(MysqlConnection connection, final String binlogfilename) throws IOException {
        final LogPosition logPosition = new LogPosition();
        connection.reconnect(); // 做下重连

        // 从该文件的第一个position开始seek数据
        connection.seek(binlogfilename, 4L, new SinkFunction<LogEvent>() {
            public boolean sink(LogEvent event) {
                CanalEntry.Entry entry = binlogParser.parse(event);
                if (entry == null) {
                    return true;
                }
                CanalEntry.Header header = entry.getHeader();

                // 获取到第一条事务开始后结束事件就返回
                if (CanalEntry.EntryType.TRANSACTIONBEGIN.equals(entry.getEntryType()) || CanalEntry.EntryType.TRANSACTIONEND.equals(entry.getEntryType())) {
                    if (CanalEntry.EntryType.TRANSACTIONEND.equals(entry.getEntryType())) {
                        logPosition.setPostion(new EntryPosition(header.getLogfileName(), header.getLogfileOffset() + event.getEventLen(), header.getExecuteTime()));
                    } else if (CanalEntry.EntryType.TRANSACTIONBEGIN.equals(entry.getEntryType())) {
                        logPosition.setPostion(new EntryPosition(header.getLogfileName(), header.getLogfileOffset(), header.getExecuteTime()));
                    }
                    return false;
                } else {
                    return true;
                }
            }
        });

        return logPosition.getPostion();
    }

    private EntryPosition findFirstPosition(MysqlConnection connection) {
        try {
            ResultSetPacket packet = connection.query("show binlog events limit 1");
            List<String> fields = packet.getFieldValues();
            if (CollectionUtils.isEmpty(fields)) {
                throw new CanalParseException("command : 'show binlog events limit 1' has an error! pls check. you need (at least one of) the SUPER,REPLICATION CLIENT privilege(s) for this operation");
            }
            EntryPosition endPosition = new EntryPosition(fields.get(0), Long.valueOf(fields.get(1)));
            return endPosition;
        } catch (Exception e) {
            throw new CanalParseException("command : 'show binlog events limit 1' has an error!", e);
        }
    }

    public EntryPosition findEndPosition(MysqlConnection connection) {
        try {
            ResultSetPacket packet = connection.query("show master status");
            List<String> fields = packet.getFieldValues();
            if (CollectionUtils.isEmpty(fields)) {
                throw new CanalParseException("command : 'show master status' has an error! pls check. you need (at least one of) the SUPER,REPLICATION CLIENT privilege(s) for this operation");
            }
            EntryPosition endPosition = new EntryPosition(fields.get(0), Long.valueOf(fields.get(1)));
            return endPosition;
        } catch (Exception e) {
            throw new CanalParseException("command : 'show master status' has an error!", e);
        }
    }

    private class DefaultSinkFunction implements SinkFunction<LogEvent> {
        private AtomicLong lastPrintTime = new AtomicLong(0);
        private InetSocketAddress remoteAddr;

        public DefaultSinkFunction(InetSocketAddress remoteAddr) {
            this.remoteAddr = remoteAddr;
        }

        public boolean sink(LogEvent event) {
            try {
                CanalEntry.Entry entry = binlogParser.parse(event);
                if (entry != null && entry.getEntryType() != EntryType.TRANSACTIONBEGIN && entry.getEntryType() != EntryType.TRANSACTIONEND) {
                    // 10秒打印一次
                    CanalEntry.Header header = entry.getHeader();
                    Long logposTimestamp = header.getExecuteTime();
                    long curTimestamp = System.currentTimeMillis();
                    if (curTimestamp > lastPrintTime.get()) {
                        lastPrintTime.set(curTimestamp + 10000);
                        DEBUG_LOG.info("dump from {}, {}", remoteAddr, DATE_FORMAT.format(new Date(logposTimestamp)));
                    }

                    // 判断是否到达终点(endTimestamp是都到达)
                    if (endTimestamp != null && logposTimestamp >= endTimestamp) {
                        LOG.info("dump end from {}, {}", remoteAddr, DATE_FORMAT.format(new Date(logposTimestamp)));
                        DEBUG_LOG.info("dump end from {}, {}", remoteAddr, DATE_FORMAT.format(new Date(logposTimestamp)));
                        return false;
                    }

                    // 判断是否到达终点(endPosition是都到达)
                    String logfilename = header.getLogfileName();
                    Long logfileoffset = header.getLogfileOffset();
                    if (endPosition != null && StringUtils.equals(endPosition.getJournalName(), logfilename) && endPosition.getPosition() <= (logfileoffset + event.getEventLen())) {
                        LOG.info("dump end from {}, {}:{}, {}", remoteAddr, logfilename, logfileoffset, DATE_FORMAT.format(new Date(logposTimestamp)));
                        DEBUG_LOG.info("dump end from {}, {}:{}, {}", remoteAddr, logfilename, logfileoffset, DATE_FORMAT.format(new Date(logposTimestamp)));
                        return false;
                    }

                    // 处理回调，并继续往前找
                    entryHandler.handle(entry);
                    return true;
                }
                return true;
            } catch (Exception e) {
                throw new CanalParseException(e);
            }
        }
    }

    // ===================== setter / getter ========================

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStartPosition(EntryPosition startPosition) {
        this.startPosition = startPosition;
    }

    public EntryPosition getStartPosition() {
        return startPosition;
    }

    public EntryPosition getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(EntryPosition endPosition) {
        this.endPosition = endPosition;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public EntryHandler getEntryHandler() {
        return entryHandler;
    }

    public void setEntryHandler(EntryHandler entryHandler) {
        this.entryHandler = entryHandler;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

}
