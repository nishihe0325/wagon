package com.youzan.wagon.console.controller.canal;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.otter.canal.common.protocol.*;
import com.alibaba.otter.canal.common.WagonConstants;
import com.youzan.wagon.console.CmdbService;
import com.youzan.wagon.console.RuleManager;
import com.youzan.wagon.console.bean.HostDetail;
import com.youzan.wagon.persistent.model.*;
import com.youzan.wagon.persistent.service.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangguofeng since 2016年1月8日 下午3:35:10
 */
@Controller
public class CanalManageController {
    private static final Logger LOG = LoggerFactory.getLogger(CanalManageController.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);
    private static final SimpleDateFormat VersionDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    // ================================= 存储 服务 ========================================
    @Resource()
    private CanalServiceService canalServiceService;
    @Resource()
    private CanalInstanceService canalInstanceService;
    @Resource()
    private CanalServerService canalServerService;
    @Resource()
    private CanalInstanceInfoService canalInstanceInfoService;
    @Resource
    private CanalClientPositionInfoService canalClientPositionInfoService;

    // ================================ 其他 服务 ==========================================
    @Resource()
    private RuleManager ruleManager;
    @Resource()
    private CmdbService cmdbService;

    // ================================ request(new)=======================================

    /**
     * 捕获到任何异常,或服务不存在,则返回"failed"字符串,因此proxy需要对这种情况做处理。
     *
     * @param serviceName 服务名称,唯一
     * @return
     */
    @RequestMapping(value = WagonConstants.URL_GET_CANAL_SERVICE_DATAS, method = RequestMethod.GET)
    @ResponseBody
    public String getCanalServiceDatas(String serviceName) {
        try {
            LOG_HEARTBEAT.info("received getCanalServiceDatas rqeuest, serviceName:{}", serviceName);
            CanalService canalService = canalServiceService.findByServiceName(serviceName);
            if (canalService == null) {
                LOG_ERROR.error("there has no service for {}", serviceName);
                return WagonConstants.CHAR_FAILED;
            }

            return new CanalServiceData(canalService.getServiceName(), canalService.getRuleBizName()).toJson();
        } catch (Throwable e) {
            LOG_ERROR.error("handle getCanalServiceDatas rqeuest failed, serviceName:{}, cause by:{}", serviceName, ExceptionUtils.getFullStackTrace(e));
            return WagonConstants.CHAR_FAILED;
        }
    }

    /**
     * 捕获到任何异常,或服务不存在,则返回"failed"字符串,因此proxy需要对这种情况做处理。
     * <p>
     * 1,暂时以canalService的modifyTime作为版本号
     * 2,暂时取所有,并没有区分实例是否暂停,挂起等状态
     * 3,如果取不到任何实例,暂时返回空
     *
     * @param serviceName
     * @return
     */
    @RequestMapping(value = WagonConstants.URL_GET_CANAL_INSTANCE_DATAS, method = RequestMethod.GET)
    @ResponseBody
    public String getCanalInstanceDatas(String serviceName) {
        try {
            CanalService canalService = canalServiceService.findByServiceName(serviceName);
            if (canalService == null) {
                LOG_ERROR.error("there has no service for {}", serviceName);
                return WagonConstants.CHAR_FAILED;
            }

            String serviceVersionNo = VersionDateFormat.format(canalService.getModifyTime());
            CanalInstanceWrapperData wrapperData = new CanalInstanceWrapperData(serviceName, serviceVersionNo);
            List<CanalInstance> instances = canalInstanceService.findByServiceName(serviceName);
            instances = instances != null ? instances : new ArrayList<CanalInstance>();
            for (CanalInstance instance : instances) {
                CanalInstanceDataV2 data = new CanalInstanceDataV2();

                data.setInstanceName(instance.getInstanceName());
                data.setDbHost(instance.getDbHost());
                data.setDbPort(instance.getDbPort());
                data.setDbUsername(instance.getDbUsername());
                data.setDbPassword(instance.getDbPassword());
                if (instance.getSlaveId() != null) {
                    data.setSlaveId(instance.getSlaveId());
                }

                data.setBinlogFile(instance.getBinlogFile());
                data.setBinlogOffset(instance.getBinlogOffset());
                data.setBinlogExeTime(instance.getBinlogExeTime());

                wrapperData.addInstanceData(data);
            }

            return wrapperData.toJson();
        } catch (Throwable e) {
            LOG_ERROR.error("handle getCanalInstanceDatas rqeuest failed, serviceName:{}, cause by:{}", serviceName, ExceptionUtils.getFullStackTrace(e));
            return WagonConstants.CHAR_FAILED;
        }
    }

    @RequestMapping(value = WagonConstants.URL_UPDATE_INSTANCE_DATAS, method = RequestMethod.POST)
    @ResponseBody
    public void updateInstanceDatas(@RequestBody String reqMsg) {
        try {
            // 查找服务
            LOG_HEARTBEAT.info("receive updateInstanceDatas request, reqMsg=[{}]", reqMsg);
            CanalInstanceWrapperData wrapperData = CanalInstanceWrapperData.fromJson(reqMsg, CanalInstanceWrapperData.class);
            String serviceName = wrapperData.getServiceName();
            if (StringUtils.isBlank(serviceName)) {
                LOG_ERROR.error("serviceName can't be blank, reqMsg=[{}]", reqMsg);
                return;
            }
            CanalService canalService = canalServiceService.findByServiceName(serviceName);
            if (canalService == null) {
                LOG_ERROR.error("no service for [{}], reqMsg=[{}]", serviceName, reqMsg);
                return;
            }

            // 更新位点信息
            for (Map.Entry<String, CanalInstanceDataV2> entry : wrapperData.getInstanceDatas().entrySet()) {
                String instanceName = entry.getKey();
                CanalInstanceDataV2 data = entry.getValue();
                CanalInstance canalInstance = canalInstanceService.findByServiceNameAndInstanceName(serviceName, instanceName);
                if (canalInstance != null) {
                    canalInstance.setBinlogFile(data.getBinlogFile());
                    canalInstance.setBinlogOffset(data.getBinlogOffset());
                    canalInstance.setBinlogExeTime(data.getBinlogExeTime());
                    canalInstance.setDbHostName(getHostName(data.getDbHost()));
                    canalInstanceService.updateByServiceNameAndInstanceName(canalInstance);
                } else {
                    LOG_ERROR.error("no instance for serviceName:{}, instanceName:{}", serviceName, instanceName);
                }
            }

            // 更新(或新建)服务器信息(启动该实例的canal进程)
            if (StringUtils.isNotBlank(wrapperData.getHost()) && wrapperData.getPort() != null) {
                String hostName = getHostName(wrapperData.getHost());
                boolean update = false;
                List<CanalServer> canalServers = canalServerService.findByServiceName(serviceName);
                canalServers = canalServers != null ? canalServers : new ArrayList<CanalServer>();
                for (CanalServer canalServer : canalServers) {
                    if (wrapperData.getHost().equals(canalServer.getHost()) && wrapperData.getPort().equals(canalServer.getPort())) {
                        canalServerService.updateModifyTime(canalServer.getId());
                        update = true;
                    }
                }
                if (!update) {
                    CanalServer canalServer = new CanalServer(serviceName, wrapperData.getHost(), hostName, wrapperData.getPort());
                    canalServerService.insert(canalServer);
                }
            }
        } catch (Throwable e) {
            LOG_ERROR.error("handle updateInstanceDatas rqeuest failed, reqMsg=[{}], cause by:{}", reqMsg, ExceptionUtils.getFullStackTrace(e));
            return;
        }
    }

    /**
     * 捕获到任何异常，则返回字符串"failed"，没有找到，则返回空json字符串。proxy需要对这两种情况做合适的处理。
     *
     * @param req
     * @return
     */
    @RequestMapping(value = WagonConstants.URL_SYNC_RULES, method = RequestMethod.GET)
    @ResponseBody
    public String syncRules(HttpServletRequest req) {
        try {
            return ruleManager.getRuleJson(req.getParameter(WagonConstants.KEY_BIZ_NAME));
        } catch (Exception e) {
            LOG_ERROR.error("syncRules failed, cause by: {}", ExceptionUtils.getFullStackTrace(e));
            return "failed";
        }
    }

    // ================================== request(old)======================================
    @RequestMapping(value = WagonConstants.URL_GET_INSTANCE_INFO, method = RequestMethod.GET)
    @ResponseBody
    public String getInstanceInfo(String canalHost, Integer canalPort, String destination) {
        LOG.info("receive getInstanceInfo request:[canalHost={}, canalPort={}, destination={}]", canalHost, canalPort, destination);

        GetInstanceInfoResult result = new GetInstanceInfoResult(canalHost, canalPort);
        try {
            List<CanalInstanceInfo> infos = null;
            if (StringUtils.isNotBlank(canalHost) && canalPort != null && StringUtils.isBlank(destination)) {
                infos = canalInstanceInfoService.findByHostAndPortAndDest(canalHost, canalPort, destination);
            } else if (StringUtils.isNotBlank(canalHost) && canalPort != null && StringUtils.isBlank(destination)) {
                infos = canalInstanceInfoService.findByHostAndPortAndDest(canalHost, canalPort, destination);
            }

            if (StringUtils.isBlank(destination)) {
                // 查找canal的全部实例
                infos = canalInstanceInfoService.findByHostAndPort(canalHost, canalPort);
            } else {
                // 只查找destination对应实例
            }

            // 放入实例信息
            if (!CollectionUtils.isEmpty(infos)) {
                for (CanalInstanceInfo info : infos) {
                    result.addInstanceData(buildCanalInstanceData(info));
                }
            }
        } catch (Exception e) {
            LOG.error("handle getInstanceInfo failed, canalHost={}, canalPort={}, destination={}, \n{}", canalHost, canalPort, destination, ExceptionUtils.getFullStackTrace(e));
            result.setResultCode(RegisterCanalInstanceResult.RESULT_CODE_FAILED);
            result.setResultMsg("shiva handle getInstanceInfo failed.");
        }

        String resultStr = result.toJson();
        LOG.info("response getInstanceInfo result:{}", resultStr);
        return resultStr;
    }

    @RequestMapping(value = WagonConstants.URL_REGISTER_INSTANCE, method = RequestMethod.POST)
    @ResponseBody
    public String registerOrUpdateInstanceInfo(@RequestBody String json) {
        LOG_HEARTBEAT.info("receive registerInstance request:{}", json);

        RegisterCanalInstanceResult result = new RegisterCanalInstanceResult();
        try {
            // decode
            CanalInstanceData data = CanalInstanceData.fromJson(json, CanalInstanceData.class);

            // 验证参数是否齐全
            String errorMsg = checkRegisterInstanceInfo(data);
            if (StringUtils.isNotBlank(errorMsg)) {
                String msg = String.format("handle registerInstance failed, request data:%s, cause:%s", json, errorMsg);
                LOG.error(msg);
                result.setResultCode(RegisterCanalInstanceResult.RESULT_CODE_FAILED);
                result.setResultMsg(msg);
            }

            // 查询当前实例(由canalHost，canalPort，destination唯一确定)
            List<CanalInstanceInfo> instances = canalInstanceInfoService.findByHostAndPortAndDest(data.getCanalHost(), data.getCanalPort(), data.getDestination());

            if (CollectionUtils.isEmpty(instances)) {
                // 实例不存在，插入实例信息
                canalInstanceInfoService.insert(buildCanalInstanceInfo(data));
            } else {
                // 实例已存在，更新最新修改时间
                canalInstanceInfoService.updateDBInfo(instances.get(0));
            }
        } catch (Exception e) {
            LOG.error("handle registerInstance failed, request data:{},\n{}", json, ExceptionUtils.getFullStackTrace(e));
            result.setResultCode(RegisterCanalInstanceResult.RESULT_CODE_FAILED);
            result.setResultMsg("shiva handle registerInstance failed.");
        }

        String resultStr = result.toJson();
        LOG_HEARTBEAT.info("response registerInstance result:{}", result);
        return resultStr;
    }

    @RequestMapping(value = WagonConstants.URL_UPDATE_POSITION, method = RequestMethod.POST)
    @ResponseBody
    public String registerOrUpdatePosition(@RequestBody String json) {
        LOG_HEARTBEAT.info("receive updatePosition request:{}", json);

        try {
            // decode
            CanalClientPositionData data = CanalClientPositionData.fromJson(json, CanalClientPositionData.class);

            // 更新所有实例的所有客户端位点
            for (Map.Entry<String, List<CanalClientPositionData.ClientPositionData>> entry : data.getPositionDatas().entrySet()) {
                // 更新该实例的所有客户端位点
                for (CanalClientPositionData.ClientPositionData clientPositionData : entry.getValue()) {
                    CanalClientPositionInfo position = buildCanalClientPosition(data.getCanalHostName(), data.getCanalHost(), data.getCanalPort(), entry.getKey(), clientPositionData);
                    // 更新或插入该实例的该客户端位点
                    if (CollectionUtils.isEmpty(canalClientPositionInfoService.findByCH_CP_Dest_ClientId(position))) {
                        canalClientPositionInfoService.insert(position); // 插入新的客户端位点
                    } else {
                        canalClientPositionInfoService.updatePosition(position); // 更新客户端位点
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("updatePositions failed, request data:{},\n{}", json, ExceptionUtils.getFullStackTrace(e));
        }

        return null;
    }

    // ================== helper method ===================
    private CanalInstanceInfo buildCanalInstanceInfo(CanalInstanceData data) {
        CanalInstanceInfo info = new CanalInstanceInfo();

        info.setCanalHostName(data.getCanalHostName());
        info.setCanalHost(data.getCanalHost());
        info.setCanalPort(data.getCanalPort());

        info.setDestination(data.getDestination());
        info.setSlaveId(data.getSlaveId());

        info.setDbHost(data.getDbHost());
        info.setDbPort(data.getDbPort());
        info.setDbUsername(data.getDbUsername());

        return info;
    }

    private CanalInstanceData buildCanalInstanceData(CanalInstanceInfo info) {
        CanalInstanceData data = new CanalInstanceData();

        data.setCanalHostName(info.getCanalHostName());
        data.setCanalHost(info.getCanalHost());
        data.setCanalPort(info.getCanalPort());

        data.setDestination(info.getDestination());
        data.setSlaveId(info.getSlaveId());

        data.setDbHost(info.getDbHost());
        data.setDbPort(info.getDbPort());
        data.setDbUsername(info.getDbUsername());

        return data;
    }

    private CanalClientPositionInfo buildCanalClientPosition(String canalHostName, String canalHost, Integer canalPort, String destination, CanalClientPositionData.ClientPositionData data) {
        CanalClientPositionInfo position = new CanalClientPositionInfo();
        position.setCanalHostName(canalHostName);
        position.setCanalHost(canalHost);
        position.setCanalPort(canalPort);
        position.setDestination(destination);
        position.setClientId(data.getClientId());
        position.setJournalName(data.getJournalName());
        position.setPosition(data.getPosition());
        position.setTimestamp(data.getTimestamp());
        return position;
    }

    private String checkRegisterInstanceInfo(CanalInstanceData data) {
        String errorMsg = "register instance data is invalid, %s should not be blank, destination:" + data.getDestination();

        if (StringUtils.isBlank(data.getCanalHost())) {
            return String.format(errorMsg, "canalHost");
        }

        if (data.getCanalPort() == null) {
            return String.format(errorMsg, "canalPort");
        }

        if (StringUtils.isBlank(data.getDestination())) {
            return String.format(errorMsg, "destination");
        }

        if (data.getSlaveId() == null) {
            return String.format(errorMsg, "slaveId");
        }

        if (StringUtils.isBlank(data.getDbHost())) {
            return String.format(errorMsg, "dbHost");
        }

        if (data.getDbPort() == null) {
            return String.format(errorMsg, "dbPort");
        }

        return null;
    }

    private String getHostName(String ip) {
        HostDetail hostDetail = cmdbService.getHostByIp(ip);
        return hostDetail != null ? hostDetail.getName() : null;
    }

}
