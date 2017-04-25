package com.youzan.wagon.deployer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.otter.canal.common.ManagerClient;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.common.WagonConstants;
import com.youzan.wagon.common.WagonException;
import com.alibaba.otter.canal.common.protocol.*;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.common.utils.NamedThreadFactory;
import com.alibaba.otter.canal.deployer.CanalController;
import com.alibaba.otter.canal.common.protocol.CanalClientPositionData.ClientPositionData;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.meta.CanalMetaManager;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.protocol.position.Position;

/**
 * canal调度控制器扩展
 *
 * @author wangguofeng since 2016年2月23日 下午1:37:52
 */
public class CanalControllerExtend extends CanalController {
    private static final Logger LOG = LoggerFactory.getLogger(CanalControllerExtend.class);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private boolean managedByConsole = true;
    private boolean allowRepeatInstance = false;
    private int heartbeatIntervalInSecond = 5;
    private String hostName;
    private String consoleAddr;
    private String ruleBizName;

    private ManagerClient managerClient = null;
    private GetInstanceInfoResult getInstanceInfoResult;

    private ScheduledExecutorService scheduledExecutorService;

    public CanalControllerExtend(ManagerClient managerClient) {
        super(PropertiesManager.getProperties());

        // hostName
        hostName = AddressUtils.getHostName();

        // properties
        managedByConsole = PropertiesManager.getBoolean(WagonConstants.PRO_MANAGED_BY_CONSOLE, true);
        allowRepeatInstance = PropertiesManager.getBoolean(WagonConstants.PRO_CANAL_INSTANCE_ALLOW_REPEAT, false);
        heartbeatIntervalInSecond = PropertiesManager.getInteger(WagonConstants.PRO_CANAL_SHIVA_HEARTBEAT_INTERVAL, 5);
        consoleAddr = PropertiesManager.getProperty(WagonConstants.PRO_CONSOLE_ADDRESS);
        ruleBizName = PropertiesManager.getProperty(WagonConstants.PRO_BIZ_NAME);

        print(); // 打印配置

        if (managedByConsole) {
            if (StringUtils.isBlank(consoleAddr) || StringUtils.isBlank(ruleBizName)) {
                throw new WagonException(String.format("some property can't be blank, such as:[%s], [%s]", WagonConstants.PRO_CONSOLE_ADDRESS, WagonConstants.PRO_BIZ_NAME));
            }
            this.managerClient = managerClient;
            scheduledExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("WagonScheduledThread_"));
        }

        // 重构并重新赋值实例生成器
        instanceGenerator = new CanalInstanceGeneratorExtend(instanceGenerator);
        embededCanalServer.setCanalInstanceGenerator(instanceGenerator);
    }

    public void start() throws Throwable {
        // 从shiva获取该canal对应的所有实例信息(首次启动时为空)
        if (managedByConsole) {
            getInstanceInfoResult = managerClient.getCanalInstanceDatas(ip, port, null);
            getInstanceInfoResult = getInstanceInfoResult != null ? getInstanceInfoResult : new GetInstanceInfoResult(ip, port);
            if (getInstanceInfoResult.getResultCode() == GetInstanceInfoResult.RESULT_CODE_SUCCESS) {
                LOG.info("get instance info successful.");
                if (getInstanceInfoResult.isNotEmpty()) {
                    StringBuilder buffer = new StringBuilder("get instance info result:");
                    for (CanalInstanceData data : getInstanceInfoResult.getInstanceDatas().values()) {
                        buffer.append("\n").append(data.getDestination()).append(":").append(data.toJson());
                    }
                    LOG.info(buffer.toString());

                    // 检测每个实例信息是否齐全
                    for (CanalInstanceData data : getInstanceInfoResult.getInstanceDatas().values()) {
                        checkCanalInstanceData(data);
                    }
                } else {
                    LOG.info("get instance info result is empty.");
                }
            } else {
                throw new WagonException(String.format("get instance info failed, ", getInstanceInfoResult.getResultMsg()));
            }
        }

        // 服务方法
        super.start();

        // 启动定时器服务
        startScheduler();
    }

    public void stop() throws Throwable {
        super.stop();

        // 停止执行器
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }

    public void startScheduler() {
        if (managedByConsole) {
            // 注册或更新实例信息
            scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    try {
                        registerOrUpdateInstanceInfo();
                    } catch (Throwable e) {
                        LOG_ERROR.error("register instance to shiva failed:{}\n", ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }, 0, heartbeatIntervalInSecond, TimeUnit.SECONDS);

            // 注册或更新消费信息
            scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    try {
                        registerOrUpdatePosition();
                    } catch (Throwable e) {
                        LOG_ERROR.error("update position to shiva failed:{}\n", ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }, 0, heartbeatIntervalInSecond, TimeUnit.SECONDS);
        }
    }

    private CanalInstance afterInstanceGenerated(CanalInstance canalInstance) throws Exception {
        if (!managedByConsole) {
            return canalInstance; // 配置为不受shiva管理
        }

        // 从shiva中获取当前实例信息
        CanalInstanceData instanceData = getInstanceInfoResult.getInstanceData(canalInstance.getDestination());
        if (instanceData == null) {
            // 第一次启动，先注册，然后返回该实例
            LOG.info("first start for instance [{}], need register first.", canalInstance.getDestination());

            instanceData = buildCanalInstanceData(canalInstance);
            RegisterCanalInstanceResult result = managerClient.registerCanalInstance(instanceData); // 注册实例
            if (result == null || result.getResultCode() != RegisterCanalInstanceResult.RESULT_CODE_SUCCESS) { // 注册失败
                String error = String.format("register instance failed for destination:[%s], cause by: %s", canalInstance.getDestination(), result.getResultMsg());
                LOG.error(error);
                throw new WagonException(error);
            }

            LOG.info("register instance successful for instance:[{}]", canalInstance.getDestination());
            return canalInstance; // 直接返回
        } else {
            // 重启的情况
            LOG.info("cover instance [{}] attribute using info from shiva.", canalInstance.getDestination());

            // 覆盖canalInstance的相关属
            MysqlEventParser mysqlEventParser = (MysqlEventParser) canalInstance.getEventParser();
            AuthenticationInfo masterInfo = mysqlEventParser.getMasterInfo();
            masterInfo.setAddress(new InetSocketAddress(instanceData.getDbHost(), instanceData.getDbPort()));
            if (StringUtils.isNotBlank(instanceData.getDbUsername())) {
                masterInfo.setUsername(instanceData.getDbUsername());
            }
            if (StringUtils.isNotBlank(instanceData.getDbPassword())) {
                masterInfo.setPassword(instanceData.getDbPassword());
            }

            return canalInstance;
        }
    }

    private CanalInstanceData buildCanalInstanceData(CanalInstance canalInstance) throws Exception {
        CanalInstanceData data = new CanalInstanceData();

        // 对应的canal信息
        data.setCanalHostName(hostName);
        data.setCanalHost(ip);
        data.setCanalPort(port);

        // 实例信息
        data.setDestination(canalInstance.getDestination());
        data.setSlaveId(((MysqlEventParser) canalInstance.getEventParser()).getSlaveId());

        // 监听的mysql信息
        AuthenticationInfo masterInfo = ((MysqlEventParser) canalInstance.getEventParser()).getMasterInfo();
        data.setDbHost(masterInfo.getAddress().getHostName());
        data.setDbPort(masterInfo.getAddress().getPort());
        data.setDbUsername(masterInfo.getUsername());
        data.setDbPassword(masterInfo.getPassword());

        return data;
    }

    private void checkCanalInstanceData(CanalInstanceData data) throws WagonException {
        if (StringUtils.isBlank(data.getDbHost())) {
            throw new WagonException("canal instance data is invalid, dbHost should not be blank, destination:{}", data.getDestination());
        }

        if (data.getDbPort() == null) {
            throw new WagonException("canal instance data is invalid, dbPort should not be null, destination:{}", data.getDestination());
        }
    }

    /**
     * 检查是否有重复实例，如果有，且配置为不允许重复，则抛出异常。重复实例是指，两个或多个实例指向同一个mysql实例
     */
    private void checkRepeatedInstance(CanalInstance canalInstance) {
        MysqlEventParser eventParser = (MysqlEventParser) canalInstance.getEventParser();
        for (CanalInstance thisInstance : embededCanalServer.getCanalInstances().values()) {
            MysqlEventParser thisEventParser = (MysqlEventParser) thisInstance.getEventParser();
            if (thisEventParser.getRunningInfo().getAddress().equals(eventParser.getMasterInfo().getAddress())) {
                // 目前暂未检测standbyInfo是否相同，只检测masterInfo是否相同
                String msg = String.format("exist repeated canal instance, %s mysql address:%s, and %s mysql address:%s", thisInstance.getDestination(), thisEventParser.getRunningInfo().getAddress(), canalInstance.getDestination(), eventParser.getMasterInfo().getAddress());
                if (allowRepeatInstance) {
                    LOG.warn(msg);
                } else {
                    throw new WagonException(msg);
                }
            }
        }
    }

    private void registerOrUpdateInstanceInfo() {
        for (CanalInstance canalInstance : embededCanalServer.getCanalInstances().values()) {
            try {
                CanalInstanceData data = buildCanalInstanceData(canalInstance);
                RegisterCanalInstanceResult result = managerClient.registerCanalInstance(data);
                if (result == null || result.getResultCode() != RegisterCanalInstanceResult.RESULT_CODE_SUCCESS) {
                    LOG_HEARTBEAT.error("register instance to shiva failed, destination:[{}], cause by:{}\n", canalInstance.getDestination(), result.getResultMsg());
                } else {
                    LOG_HEARTBEAT.info("register instance to shiva sucessful, destination:[{}]", canalInstance.getDestination());
                }
            } catch (Exception e) {
                LOG_HEARTBEAT.error("register instance to shiva failed, destination:[{}] \n{}", canalInstance.getDestination(), ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    private void registerOrUpdatePosition() throws Exception {
        CanalClientPositionData data = new CanalClientPositionData(hostName, ip, port);

        // 添加每个实例的每个客户端位点信息
        for (CanalInstance canalInstance : embededCanalServer.getCanalInstances().values()) {
            CanalMetaManager metaManager = canalInstance.getMetaManager();
            // 添加该实例的所有客户端位点信息
            List<ClientIdentity> allClients = metaManager.listAllSubscribeInfo(canalInstance.getDestination());
            if (!CollectionUtils.isEmpty(allClients)) {
                for (ClientIdentity clientIdentity : allClients) {
                    // 添加该实例的该客户端位点信息
                    ClientPositionData clientData = new ClientPositionData(clientIdentity.getClientId());
                    data.addPositionData(canalInstance.getDestination(), clientData);
                    Position logPosition = metaManager.getCursor(clientIdentity);
                    if (logPosition != null) {// 为null的情况：client还没有ack过
                        EntryPosition entryPosition = ((LogPosition) logPosition).getPostion();
                        clientData.setJournalName(entryPosition.getJournalName());
                        clientData.setPosition(entryPosition.getPosition());
                        clientData.setTimestamp(entryPosition.getTimestamp());
                    }
                }
            }
        }
        managerClient.updatePosition(data);
        LOG_HEARTBEAT.info("update position to shiva sucessful, data:{}\n", data.toJson());
    }

    private void print() {
        LOG.info("{}={}", WagonConstants.PRO_MANAGED_BY_CONSOLE, managedByConsole);
        LOG.info("{}={}", WagonConstants.PRO_CANAL_INSTANCE_ALLOW_REPEAT, allowRepeatInstance);
        LOG.info("{}={}", WagonConstants.PRO_CANAL_SHIVA_HEARTBEAT_INTERVAL, heartbeatIntervalInSecond);
        LOG.info("{}={}", WagonConstants.PRO_CONSOLE_ADDRESS, consoleAddr);
        LOG.info("{}={}", WagonConstants.PRO_BIZ_NAME, ruleBizName);
    }

    // ================================class =================================
    private class CanalInstanceGeneratorExtend implements CanalInstanceGenerator {

        private CanalInstanceGenerator instanceGeneratorInner;

        public CanalInstanceGeneratorExtend(CanalInstanceGenerator instanceGeneratorInner) {
            this.instanceGeneratorInner = instanceGeneratorInner;
        }

        public CanalInstance generate(String destination) {
            CanalInstance canalInstance = instanceGeneratorInner.generate(destination);
            checkRepeatedInstance(canalInstance); // 检查是否有重复实例

            // 扩展处理
            try {
                return afterInstanceGenerated(canalInstance);
            } catch (Exception e) {
                String msg = String.format("generate CanalInstance failed for destination:[%s].", destination);
                LOG.error(msg, e);
                throw new WagonException(msg, e);
            }
        }
    }

}
