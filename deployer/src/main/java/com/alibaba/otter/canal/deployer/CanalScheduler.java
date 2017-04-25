package com.alibaba.otter.canal.deployer;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.parse.CanalEventParser;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;


public class CanalScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(CanalScheduler.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger("ERROR");

    private final Properties properties;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "CanalSchedulerThread-" + count.getAndIncrement());
        }
    });

    public CanalScheduler(Properties properties) {
        this.properties = properties;
    }

    // ======================================================================================
    public void start() {
        // mysql 心跳检测,长时间没有mysql dump心跳包,则重启
        if (!"false".equals(getProperty(properties, "mysql.dump.heartbeat"))) {
            startMysqlHeartbeatCheckSchedule();
        }
    }

    public void stop() {
    }

    private void startMysqlHeartbeatCheckSchedule() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    CanalServerWithEmbedded canalServer = CanalServerWithEmbedded.instance();
                    Map<String, CanalInstance> instances = canalServer.getCanalInstances();
                    if (CollectionUtils.isEmpty(instances)) {
                        return;
                    }


                    for (CanalInstance instance : instances.values()) {
                        MysqlEventParser eventParser = (MysqlEventParser) instance.getEventParser();
                        if (System.currentTimeMillis() > eventParser.getLastDumpTime() + 3 * 10 * 1000) {
                            // 重启实例(还是重启Parser?)
                            MDC.put("destination", "");
                            String destination = instance.getDestination();
                            try {
                                LOG.info("restarting instance for destination {} ...", destination);
                                canalServer.stop(destination);
                                canalServer.start(destination);
                                LOG.info("restart instance for destination {} success.", destination);
                            } catch (Exception e) {
                                LOG.error("restart instance for destination {} failed, cause by:{}", destination, ExceptionUtils.getFullStackTrace(e));
                            }
                            MDC.remove("destination");
                        }
                    }
                } catch (Exception e) {
                    LOG.error("do mysqlHeartbeatCheckSchedule failed, cause by:{}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private String getProperty(Properties properties, String key) {
        return StringUtils.trim(properties.getProperty(StringUtils.trim(key)));
    }
}
