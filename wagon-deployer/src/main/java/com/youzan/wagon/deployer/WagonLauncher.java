package com.youzan.wagon.deployer;

import com.alibaba.otter.canal.common.ManagerClient;
import com.alibaba.otter.canal.common.protocol.CanalServiceData;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.alibaba.otter.canal.deployer.CanalControllerInterface;
import com.alibaba.otter.canal.deployer.CanalControllerWithManager;
import com.alibaba.otter.canal.common.WagonConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WagonLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(WagonLauncher.class);
    private static final String CONFIG = "/canal.properties";

    public static void main(String[] args) {
        try {
            // 初始化并启动配置管理器
            PropertiesManager.configFile(CONFIG);
            PropertiesManager.start();


            // 获取服务信息
            String instanceMode = PropertiesManager.get(WagonConstants.PRO_CANAL_MODE);
            ManagerClient managerClient = new ManagerClient();
            CanalServiceData canalServiceData = null;
            if (WagonConstants.PRO_CANAL_MODE_MANAGER.equals(instanceMode)) {
                String serviceName = PropertiesManager.get(WagonConstants.PRO_SERVICE_NAME);
                canalServiceData = managerClient.getCanalServiceData(serviceName);
            }


            // 初始化控制器
            final ConsumeController consumeController = new ConsumeController();
            final CanalControllerInterface canalController //
                    = WagonConstants.PRO_CANAL_MODE_MANAGER.equals(instanceMode) ? //
                    new CanalControllerWithManager(managerClient) ://
                    new CanalControllerExtend(managerClient);

            // 设置控制器
            consumeController.setCanalServiceData(canalServiceData);


            // 启动控制器
            canalController.start();
            consumeController.start();

            // 添加关闭回调
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    Throwable throwable = null;
                    try {
                        consumeController.stop();
                    } catch (Throwable e) {
                        throwable = e;
                    }

                    try {
                        canalController.stop();
                    } catch (Throwable e) {
                        throwable = e;
                    }

                    if (throwable == null) {
                        LOG.error("stop wagon success.");
                    } else {
                        LOG.error("stop wagon failed, cause by: {}", ExceptionUtils.getFullStackTrace(throwable));
                    }
                }
            });

            LOG.info("start wagon success.");
        } catch (Throwable e) {
            LOG.error("start wagon failed, cause by: {}", ExceptionUtils.getFullStackTrace(e));
            System.exit(0);
        }
    }

}
