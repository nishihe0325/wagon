package com.alibaba.otter.canal.deployer.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.common.ManagerClient;
import com.alibaba.otter.canal.common.WagonConstants;
import com.alibaba.otter.canal.common.protocol.CanalInstanceDataV2;
import com.alibaba.otter.canal.common.protocol.CanalInstanceWrapperData;
import com.alibaba.otter.canal.common.utils.HttpClientUtil;
import com.alibaba.otter.canal.common.utils.NamedThreadFactory;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.manager.CanalInstanceContainer;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManagerInstanceConfigMonitor extends AbstractInstanceConfigMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ManagerInstanceConfigMonitor.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);

    private final String serviceName;
    private final InstanceAction instanceAction;
    private final ManagerClient managerClient;
    private final CanalServerWithEmbedded embededCanalServer = CanalServerWithEmbedded.instance();
    private final CanalInstanceContainer instanceContainer = CanalInstanceContainer.getInstance();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("canal-instance-scan"));

    // ================================= private =================================
    public ManagerInstanceConfigMonitor(String serviceName, InstanceAction instanceAction, ManagerClient managerClient) {
        this.serviceName = serviceName;
        this.instanceAction = instanceAction;
        this.managerClient = managerClient;
    }

    public void start() {
        super.start();

        executor.scheduleWithFixedDelay(() -> {
            try {
                if (updateFromManager()) { // 先从console同步数据
                    scan();                // 只有同步成功,才更新
                }
            } catch (Throwable e) {
                LOG_ERROR.error("do InstanceConfigMonitor failed, cause by:{}", ExceptionUtils.getFullStackTrace(e));
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        super.stop();
        executor.shutdownNow();
    }

    // ================================= private =================================

    private void scan() {
        Set<CanalInstanceDataV2> newInstances = new HashSet<CanalInstanceDataV2>();
        Set<CanalInstanceDataV2> stopInstances = new HashSet<CanalInstanceDataV2>();
        Set<CanalInstanceDataV2> reloadInstances = new HashSet<CanalInstanceDataV2>();
        Map<String, CanalInstance> runningInstances = embededCanalServer.getCanalInstances();
        Map<String, CanalInstanceDataV2> recentInstances = instanceContainer.getInstances();

        //  新的实例
        for (CanalInstanceDataV2 recentInstance : recentInstances.values()) {
            if (!runningInstances.containsKey(recentInstance.getInstanceName())) {
                newInstances.add(recentInstance);
            }
        }

        // 需要停止,重启的实例
        for (CanalInstance runningInstance : runningInstances.values()) {
            CanalInstanceDataV2 recentInstance = recentInstances.get(runningInstance.getDestination());
            if (recentInstance == null) {
                stopInstances.add(recentInstance);
            } else if (!instanceEquals(runningInstance, recentInstance)) {
                reloadInstances.add(recentInstance);
            }
        }

        // 打印需要操作的实例
        StringBuilder buffer = new StringBuilder();
        if (newInstances.size() > 0) {
            buffer.append("newInstances[").append(JSON.toJSONString(newInstances, false)).append("]; ");
        }
        if (stopInstances.size() > 0) {
            buffer.append("stopInstances:").append(JSON.toJSONString(stopInstances, false)).append("]; ");
        }
        if (reloadInstances.size() > 0) {
            buffer.append("reloadInstances:").append(JSON.toJSONString(reloadInstances, false)).append("]; ");
        }
        String reloadInfo = buffer.toString();
        if (StringUtils.isNotBlank(reloadInfo)) {
            LOG.info("reload instance: {}", reloadInfo);
        }

        // 启动实例
        if (!CollectionUtils.isEmpty(newInstances)) {
            for (CanalInstanceDataV2 instanceData : newInstances) {
                try {
                    instanceAction.start(instanceData.getInstanceName());
                    LOG.info("start {} successful.", instanceData.getInstanceName());
                } catch (Throwable e) {
                    LOG_ERROR.error("start {} failed, cause by:{}", instanceData.getInstanceName(), ExceptionUtils.getFullStackTrace(e));
                }
            }
        }

        // 停止实例
        if (!CollectionUtils.isEmpty(stopInstances)) {
            for (CanalInstanceDataV2 instanceData : stopInstances) {
                try {
                    instanceAction.stop(instanceData.getInstanceName());
                    LOG.info("stop {} successful.", instanceData.getInstanceName());
                } catch (Throwable e) {
                    LOG_ERROR.error("stop {} failed, cause by:{}", instanceData.getInstanceName(), ExceptionUtils.getFullStackTrace(e));
                }
            }
        }

        // 重启实例
        if (!CollectionUtils.isEmpty(reloadInstances)) {
            for (CanalInstanceDataV2 instanceData : reloadInstances) {
                try {
                    instanceAction.reload(instanceData.getInstanceName());
                    LOG.info("reload {} successful.", instanceData.getInstanceName());
                } catch (Throwable e) {
                    LOG_ERROR.error("reload {} failed, cause by:{}", instanceData.getInstanceName(), ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
    }

    /**
     * 从控制台同步实例信息,并更新到缓存容器: instanceContainer,注意点
     * <p>
     * 1, 捕获到任何异常,则返回false,这样就不会重新加载实例
     * 2, 接收到的数据为空或为"failed"字符串,则返回false,这样就不会重新加载实例
     * 3, 只有当版本有更新时,才更新缓存
     *
     * @return
     * @throws Exception
     */
    private boolean updateFromManager() {
        try {
            CanalInstanceWrapperData wrapperData = managerClient.getCanalInstanceDatas(serviceName);
            if (versionHasUpdated(instanceContainer.getVersion(), wrapperData.getVersion())) { // 版本有更新
                LOG_HEARTBEAT.info("update instance container, container size={}, container version={}, recent version={}", instanceContainer.size(), instanceContainer.getVersion(), wrapperData.getVersion());
                Map<String, CanalInstanceDataV2> latestInstances = wrapperData.getInstanceDatas();
                for (CanalInstanceDataV2 instanceData : latestInstances.values()) {
                    instanceContainer.addInstance(instanceData); // 如果存在相同实例名,则会替换为最新的
                }

                for (CanalInstanceDataV2 instanceData : instanceContainer.getInstances().values()) {
                    if (!latestInstances.containsKey(instanceData.getInstanceName())) {
                        instanceContainer.removeInstance(instanceData.getInstanceName()); // 已经不存在的,则删除
                    }
                }
                instanceContainer.setVersion(wrapperData.getVersion()); // 更新版本号
            }

            return true;
        } catch (Throwable e) {
            LOG_ERROR.error("sync instance from manager failed, cause by:{}", ExceptionUtils.getFullStackTrace(e));
            return false;
        }
    }

    // ================================= help method =================================
    private boolean versionHasUpdated(String srcVersion, String tagetVersion) {
        return srcVersion != null ? !srcVersion.equals(tagetVersion) : tagetVersion != null;
    }

    private boolean instanceEquals(CanalInstance srcInstance, CanalInstanceDataV2 tagetData) {
        MysqlEventParser mysqlEventParser = (MysqlEventParser) srcInstance.getEventParser();
        InetSocketAddress srcAddr = mysqlEventParser.getRunningInfo().getAddress();
        InetSocketAddress tagetAddr = new InetSocketAddress(tagetData.getDbHost(), tagetData.getDbPort());

        boolean equals = srcInstance.getDestination().equals(tagetData.getInstanceName()); // 实例名相等
        return equals && srcAddr.equals(tagetAddr);                                        // 数据库地址相等
    }

}
