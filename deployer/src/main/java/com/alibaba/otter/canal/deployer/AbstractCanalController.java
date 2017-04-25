package com.alibaba.otter.canal.deployer;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningData;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningListener;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitor;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitors;
import com.alibaba.otter.canal.deployer.monitor.InstanceAction;
import com.alibaba.otter.canal.deployer.monitor.InstanceConfigMonitor;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty.CanalServerWithNetty;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.ServerSocket;
import java.util.Properties;

public abstract class AbstractCanalController implements CanalControllerInterface {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCanalController.class);

    protected String ip;
    protected Integer port;

    protected final InstanceAction instanceAction;
    protected CanalInstanceGenerator instanceGenerator;
    protected InstanceConfigMonitor instanceConfigMonitor;

    protected final CanalServerWithEmbedded embededCanalServer;
    protected final CanalServerWithNetty canalServer;

    protected final CanalScheduler canalScheduler;

    // ====================== init ====================================
    public AbstractCanalController(final Properties properties) {
        String portStr = PropertiesManager.getProperty(properties, CanalConstants.CANAL_PORT);
        if (StringUtils.isNotBlank(portStr)) {
            port = Integer.valueOf(portStr);
        }

        // 处理下ip为空，默认使用hostIp暴露到zk中
        if (StringUtils.isEmpty(ip)) {
            ip = AddressUtils.getHostIp();
        }

        instanceAction = initInstanceAction();
        initServerRunningMonitors();

        // 准备canal server
        embededCanalServer = CanalServerWithEmbedded.instance();
        canalServer = CanalServerWithNetty.instance();

        canalScheduler = new CanalScheduler(properties);
    }

    private InstanceAction initInstanceAction() {
        return new InstanceAction() {
            public void start(String destination) {
                if (!embededCanalServer.isStart(destination)) {
                    // HA机制启动
                    ServerRunningMonitor runningMonitor = ServerRunningMonitors.getRunningMonitor(destination);
                    if (!runningMonitor.isStart()) {
                        runningMonitor.start();
                    }
                }
            }

            public void stop(String destination) {
                // 此处的stop，代表强制退出，非HA机制，所以需要退出HA的monitor和配置信息
                embededCanalServer.stop(destination);
                ServerRunningMonitor runningMonitor = ServerRunningMonitors.getRunningMonitor(destination);
                if (runningMonitor.isStart()) {
                    runningMonitor.stop();
                }
            }

            public void reload(String destination) {
                // 目前任何配置变化，直接重启，简单处理
                stop(destination);
                start(destination);
            }
        };
    }

    private void initServerRunningMonitors() {
        final ServerRunningData serverData = new ServerRunningData();
        ServerRunningMonitors.setServerData(serverData);

        ServerRunningMonitors.setRunningMonitors(MigrateMap.makeComputingMap(new Function<String, ServerRunningMonitor>() {
            public ServerRunningMonitor apply(final String destination) {
                ServerRunningMonitor runningMonitor = new ServerRunningMonitor(serverData);
                runningMonitor.setDestination(destination);
                runningMonitor.setListener(new ServerRunningListener() {

                    public void processActiveEnter() {
                        try {
                            MDC.put(CanalConstants.MDC_DESTINATION, String.valueOf(destination));
                            embededCanalServer.start(destination);
                        } finally {
                            MDC.remove(CanalConstants.MDC_DESTINATION);
                        }
                    }

                    public void processActiveExit() {
                        try {
                            MDC.put(CanalConstants.MDC_DESTINATION, String.valueOf(destination));
                            embededCanalServer.stop(destination);
                        } finally {
                            MDC.remove(CanalConstants.MDC_DESTINATION);
                        }
                    }

                    public void processStart() {
                    }

                    public void processStop() {
                    }

                });

                return runningMonitor;
            }
        }));

    }

    // ====================== start, stop ====================================
    public void start() throws Throwable {
        embededCanalServer.start();
        instanceConfigMonitor.start();

        canalServer.setPort(port);
        canalServer.start();

        canalScheduler.start();

        logger.info("## start the canal server[{}]", port);
    }

    public void stop() throws Throwable {
        canalScheduler.stop();
        canalServer.stop();
        instanceConfigMonitor.stop();
        embededCanalServer.stop();

        for (ServerRunningMonitor runningMonitor : ServerRunningMonitors.getRunningMonitors().values()) {
            if (runningMonitor.isStart()) {
                runningMonitor.stop();
            }
        }

        logger.info("## stop the canal server[{}]", port);
    }

}
