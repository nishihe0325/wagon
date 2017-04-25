package com.youzan.wagon.deployer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.otter.canal.common.protocol.CanalServiceData;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.processor.SyncProcessor;
import com.youzan.wagon.filter.processor.EntryProcessor;
import com.youzan.wagon.filter.processor.mq.NSQService;

public class ConsumeController {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumeController.class);

    private final NSQService nsqService;
    private CanalServiceData canalServiceData;
    private final List<SyncProcessor> processors = new ArrayList<SyncProcessor>();

    private final CanalServerWithEmbedded canalServer;
    private final Map<String/* destination */, CanalInstanceConsumeService> consumeServiceTable = new ConcurrentHashMap<String, CanalInstanceConsumeService>();
    private final ScheduledExecutorService scheduledExecutorService;

    public ConsumeController() {
        canalServer = CanalServerWithEmbedded.instance();

        // nsq处理服务
        this.nsqService = new NSQService(PropertiesManager.getProperties());

        // 定时任务服务
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            private final AtomicLong threadIndex = new AtomicLong(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ConsumeControllerScheduledThread-" + this.threadIndex.incrementAndGet());
            }
        });
    }

    public void start() throws Throwable {
        // 启动nsq服务
        nsqService.start();

        // 添加SyncProcessor
        processors.add(new EntryProcessor(nsqService));

        // 对控制台注册中心的操作，注册filter，同步规则等
        ConsoleProcessor consoleProcessor = new ConsoleProcessor(PropertiesManager.getProperties(), this);
        consoleProcessor.setCanalServiceData(canalServiceData);
        consoleProcessor.process();

        // 先扫描并启动实例
        scanInstanceConsumeService();

        // 定时扫描consumeServiceTable
        int period = PropertiesManager.getInteger(WagonConstants.PERIOD_CONSUMER_SCAN, 60);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    scanInstanceConsumeService();
                } catch (Exception e) {
                    LOG.error("scanConsumeServiceInstance failed:{}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 10, period, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduledExecutorService.shutdown();
        nsqService.stop();
    }

    private void scanInstanceConsumeService() {
        // 扫描实例，增加并启动未消费的CanalInstanceConsumeService
        Map<String, CanalInstance> instanceMap = canalServer.getCanalInstances();
        for (Map.Entry<String, CanalInstance> entry : instanceMap.entrySet()) {
            if (!consumeServiceTable.containsKey(entry.getKey())) {
                // 还未启动消费服务，则启动该消费服务
                CanalInstanceConsumeService consumeService = new CanalInstanceConsumeService(entry.getKey(), processors);
                consumeServiceTable.put(entry.getKey(), consumeService);
                consumeService.start(); // 启动消费服务
                LOG.info("start CanalInstanceConsumeService seccess, destination:{}", entry.getKey());
            }
        }

        // 删除已经停止的CanalDestinationConsumeService
        for (Map.Entry<String, CanalInstanceConsumeService> entry : consumeServiceTable.entrySet()) {
            if (!instanceMap.containsKey(entry.getKey())) {
                // 对应的instance已停止，则同时停止该服务
                CanalInstanceConsumeService consumeService = consumeServiceTable.remove(entry.getKey());
                if (consumeService != null) {
                    consumeService.stop(); // 停止消费服务
                    LOG.info("CanalInstanceConsumeService is shutdown now, destination:{}", entry.getKey());
                }
            }
        }
    }

    public Map<String, CanalInstanceConsumeService> getConsumeServiceTable() {
        return consumeServiceTable;
    }

    // ======================= get and set ==================================

    public CanalServiceData getCanalServiceData() {
        return canalServiceData;
    }

    public void setCanalServiceData(CanalServiceData canalServiceData) {
        this.canalServiceData = canalServiceData;
    }

}
