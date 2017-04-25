package com.youzan.wagon.deployer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.youzan.wagon.common.ServiceThread;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.WagonException;
import com.youzan.wagon.common.processor.SyncProcessor;

/**
 * <pre>
 * canal instance消费服务。
 * 一个该类的对象代表一个独立线程，消费某个具体的canal instance。
 * 独立从指定destination获取数据，获取到数据后，由包含的执行器执行，阻塞等待所有数据处理完成后，再执行ack或rollback。
 * </pre>
 * 
 * @author wangguofeng since 2016年6月30日 上午9:46:35
 */
public class CanalInstanceConsumeService extends ServiceThread {
    private static final Logger LOG = LoggerFactory.getLogger(CanalInstanceConsumeService.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private static final int DefaultBatchSize = 1000; // 默认每次获取最大条数
    private static final String DefaultSubscribe = WagonConstants.SUBSCRIBE_ALL; // 默认订阅全部

    private final CanalServerWithEmbedded canalServer;
    private final ClientIdentity clientIdentity;
    private String subscribe;
    private int batchSize;

    private final List<SyncProcessor> processors;
    private ExecutorService executor;

    public CanalInstanceConsumeService(String destination, List<SyncProcessor> processors) {
        this(destination, DefaultSubscribe, DefaultBatchSize, processors);
    }

    public CanalInstanceConsumeService(String destination, String subscribe, int batchSize, List<SyncProcessor> processors) {
        if (StringUtils.isBlank(destination) || StringUtils.isBlank(subscribe)) {
            throw new WagonException(String.format("destination or subscribe can't be blank,[destination=%s, subscribe=%s].", destination, subscribe));
        }
        if (processors == null || processors.size() == 0) {
            throw new WagonException("processors can't be empty.");
        }

        this.canalServer = CanalServerWithEmbedded.instance();
        this.subscribe = subscribe;
        this.clientIdentity = new ClientIdentity(destination, (short) 1001, this.subscribe);
        this.batchSize = batchSize <= 0 ? DefaultBatchSize : batchSize;
        this.processors = processors;
    }

    public void reSubscribe(String newSubscribe) {
        this.subscribe = newSubscribe;
        this.clientIdentity.setFilter(this.subscribe);
        this.canalServer.subscribe(this.clientIdentity);
    }

    @Override
    public void run() {
        canalServer.subscribe(clientIdentity);
        executor = Executors.newFixedThreadPool(processors.size());
        int processorSize = processors.size();

        // 运行消费
        while (!isStoped()) {
            try {
                doGet(processorSize);
            } catch (Exception e) {
                LOG_ERROR.error("instance consume failed, destination:{}, cause by:\n{}", clientIdentity.getDestination(), ExceptionUtils.getFullStackTrace(e));
            }
        }

        LOG.info("canalInstanceConsumeService is stoped, destination:{}", getDestination());
    }

    private void doGet(int processorSize) throws InterruptedException, ExecutionException {
        Message message = canalServer.getWithoutAck(clientIdentity, batchSize);
        long batchId = message.getId();
        List<Entry> entries = message.getEntries();

        if (batchId != -1 && entries.size() > 0) {
            List<Map<String, FutureTask<Boolean>>> tasks = new ArrayList<Map<String, FutureTask<Boolean>>>(processorSize);
            for (final SyncProcessor processor : processors) {
                FutureTask<Boolean> task = generateTask(processor, entries);
                Map<String, FutureTask<Boolean>> taskMap = new HashMap<String, FutureTask<Boolean>>(1);
                taskMap.put(processor.getClass().getSimpleName(), task);
                tasks.add(taskMap);
                executor.submit(task);
            }

            List<String> falseTasks = new ArrayList<String>(processorSize);
            for (Map<String, FutureTask<Boolean>> taskMap : tasks) {
                for (String key : taskMap.keySet()) {
                    boolean result = taskMap.get(key).get();
                    if (!result)
                        falseTasks.add(key);
                }
            }

            int fSize = falseTasks.size();
            if (fSize == 0) {// 全部处理成功
                canalServer.ack(clientIdentity, batchId);
            } else if (fSize == processorSize) {// 全部处理失败，rollback
                LOG.error("all processor goes wrong");
                canalServer.rollback(clientIdentity, batchId);
            } else {
                // 部分处理失败，记录日志，失败原因的日志由具体processor记录
                canalServer.ack(clientIdentity, batchId);
            }
        } else {
            // 没有数据，则线程暂停1秒，避免cpu被占满
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private FutureTask<Boolean> generateTask(final SyncProcessor processor, final List<Entry> entries) {
        return new FutureTask<Boolean>(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return processor.process(entries);
            }
        });
    }

    @Override
    public String getServiceName() {
        return "CanalInstanceConsumeThread";
    }

    public String getDestination() {
        return clientIdentity.getDestination();
    }

}
