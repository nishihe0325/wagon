package com.alibaba.otter.canal.deployer;

import com.alibaba.otter.canal.common.ManagerClient;
import com.alibaba.otter.canal.common.WagonConstants;
import com.alibaba.otter.canal.common.protocol.CanalInstanceDataV2;
import com.alibaba.otter.canal.common.protocol.CanalInstanceWrapperData;
import com.alibaba.otter.canal.common.utils.NamedThreadFactory;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.alibaba.otter.canal.deployer.monitor.ManagerInstanceConfigMonitor;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.manager.ManagerCanalInstanceGenerator;
import com.alibaba.otter.canal.meta.CanalMetaManager;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.protocol.position.Position;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CanalControllerWithManager extends AbstractCanalController {
    private static final Logger LOG = LoggerFactory.getLogger(CanalControllerWithManager.class);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private final String serviceName;
    private ManagerClient managerClient = null;
    private int heartbeatIntervalInSecond = 5;
    private ScheduledExecutorService scheduledExecutorService;

    // ====================== init ====================================
    public CanalControllerWithManager(ManagerClient managerClient) {
        super(PropertiesManager.getProperties());
        this.managerClient = managerClient;

        serviceName = PropertiesManager.getProperty(WagonConstants.PRO_SERVICE_NAME);
        assert StringUtils.isNotBlank(serviceName);


        instanceGenerator = ManagerCanalInstanceGenerator.getInstance();
        embededCanalServer.setCanalInstanceGenerator(instanceGenerator);
        instanceConfigMonitor = new ManagerInstanceConfigMonitor(serviceName, instanceAction, managerClient);

        scheduledExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("WagonScheduledThread_"));
    }

    public void start() throws Throwable {
        super.start();

        startScheduler(); // 启动定时器
    }

    // =================================================================================
    public void startScheduler() {
        // 更新实例信息
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    updateInstanceInfo();
                } catch (Throwable e) {
                    LOG_ERROR.error("updateInstanceInfo failed:{}\n", ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 0, heartbeatIntervalInSecond, TimeUnit.SECONDS);
    }

    private void updateInstanceInfo() throws Exception {
        try {
            CanalInstanceWrapperData data = new CanalInstanceWrapperData(serviceName, ip, port);

            // 添加每个实例信息
            for (CanalInstance canalInstance : embededCanalServer.getCanalInstances().values()) {
                CanalMetaManager metaManager = canalInstance.getMetaManager();
                List<ClientIdentity> allClients = metaManager.listAllSubscribeInfo(canalInstance.getDestination());
                if (!CollectionUtils.isEmpty(allClients)) {
                    ClientIdentity clientIdentity = allClients.get(0);
                    MysqlEventParser eventParser = (MysqlEventParser) canalInstance.getEventParser();
                    AuthenticationInfo masterInfo = ((MysqlEventParser) canalInstance.getEventParser()).getMasterInfo();

                    // 设置实例名和数据库信息
                    CanalInstanceDataV2 instanceData = new CanalInstanceDataV2();
                    instanceData.setInstanceName(canalInstance.getDestination());
                    instanceData.setSlaveId(eventParser.getSlaveId());
                    instanceData.setDbHost(masterInfo.getAddress().getAddress().getHostAddress());
                    instanceData.setDbPort(masterInfo.getAddress().getPort());

                    // 设置消费位点信息
                    Position logPosition = metaManager.getCursor(clientIdentity);
                    if (logPosition != null) {// 为null的情况：client还没有ack过
                        EntryPosition entryPosition = ((LogPosition) logPosition).getPostion();
                        instanceData.setBinlogFile(entryPosition.getJournalName());
                        instanceData.setBinlogOffset(entryPosition.getPosition());
                        instanceData.setBinlogExeTime(entryPosition.getTimestamp());
                    }

                    data.addInstanceData(instanceData);
                }
            }

            managerClient.updateInstanceDatas(data);
        } catch (Throwable e) {
            LOG_ERROR.error("updateInstanceInfo failed ,cause by: {}\n", ExceptionUtils.getFullStackTrace(e));
        }
    }

}
