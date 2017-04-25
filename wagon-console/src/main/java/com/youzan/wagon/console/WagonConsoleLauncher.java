package com.youzan.wagon.console;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.common.utils.PropertiesManager;

/**
 * wagon控制台启动类
 * 
 * @author wangguofeng since 2016年3月9日 下午2:22:47
 */
public class WagonConsoleLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(WagonConsoleLauncher.class);

    public static void main(String[] args) {
        try {
            // 启动配置管理器
            PropertiesManager.start();

            // 启动jetty
            final JettyEmbedServer server = new JettyEmbedServer();
            server.start();

            // 启动监控报警(目前不启动该调度程序，监控信息由运维定时采集)
            // MonitorScheduler monitor = new MonitorScheduler();
            // monitor.start();
            // LOG.info("MonitorScheduler is started.");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        PropertiesManager.stop();
                        // monitor.stop();
                        server.stop();
                    } catch (Throwable e) {
                        LOG.error("## Something goes wrong when stoping the wagon console:\n{}", ExceptionUtils.getFullStackTrace(e));
                    } finally {
                        LOG.info("wagon console is stoped.");
                    }
                }
            });
        } catch (Throwable e) {
            LOG.error("## Something goes wrong when starting up the wagon console:\n{}", ExceptionUtils.getFullStackTrace(e));
            System.exit(0);
        }
    }

}
