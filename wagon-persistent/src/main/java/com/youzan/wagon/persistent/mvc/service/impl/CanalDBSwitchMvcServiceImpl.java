package com.youzan.wagon.persistent.mvc.service.impl;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchInstanceResult;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchRequestData;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchResult;
import com.youzan.wagon.persistent.CanalConnectorManager;
import com.youzan.wagon.persistent.model.CanalInstanceInfo;
import com.youzan.wagon.persistent.mvc.service.CanalDBSwitchMvcService;
import com.youzan.wagon.persistent.service.CanalDBSwitchLogInfoService;
import com.youzan.wagon.persistent.service.CanalInstanceInfoService;

@Component("canalDBSwitchMvcService")
public class CanalDBSwitchMvcServiceImpl implements CanalDBSwitchMvcService {

    private static final Logger LOG = LoggerFactory.getLogger(CanalDBSwitchMvcServiceImpl.class);

    @Resource()
    private CanalInstanceInfoService canalInstanceInfoService;
    @Resource
    private CanalDBSwitchLogInfoService canalDBSwitchLogInfoService;

    public String canalDBSwitch(String json) {
        CanalDBSwitchResult result = null;

        try {
            result = doCanalDBSwitch(json);
        } catch (Exception e) {
            String errorMsg = "handle canalDBSwitch request failed.";
            LOG.error(errorMsg, e);
            result.setResultCode(CanalDBSwitchResult.RESULT_CODE_FAILED);
            result.setResultMsg(errorMsg + e.getMessage());
        }

        // 记录下切换结果，暂未实现

        alarmCanalDBSwitchFailed(json, result); // 接入报警系统

        return result.toJson();
    }

    // ================== helper method ===================

    private CanalDBSwitchResult doCanalDBSwitch(String json) {
        CanalDBSwitchResult result = new CanalDBSwitchResult();
        CanalDBSwitchRequestData data = CanalDBSwitchRequestData.fromJson(json, CanalDBSwitchRequestData.class);

        // 记录下切换请求，咱未实现

        // 已注册实例中，找不到和切换地址相应的canal实例
        List<CanalInstanceInfo> infos = canalInstanceInfoService.findByHostAndPort(data.getFromDBData().getHost(), data.getFromDBData().getPort());
        if (CollectionUtils.isEmpty(infos)) {
            String errorMsg = String.format("there are no canal info matching to the mysql, the mysql address=%s:%d", data.getFromDBData().getHost(), data.getFromDBData().getPort());
            LOG.error(errorMsg);
            result.setResultCode(CanalDBSwitchResult.RESULT_CODE_FAILED);
            result.setResultMsg(errorMsg);
            return result;
        }

        // 向涉及的canal server发送切换请求
        Set<InetSocketAddress> switchedAddresses = new HashSet<InetSocketAddress>();
        for (CanalInstanceInfo info : infos) {
            InetSocketAddress canalAddress = new InetSocketAddress(info.getCanalHost(), info.getCanalPort());
            if (switchedAddresses.contains(canalAddress)) {
                continue; // 该canal server已经执行过切换请求，不用重复执行
            }
            result.add(doCanalDBSwitch(canalAddress, data)); // 执行切换，并加入切换结果
            // 缓存已切换的canal地址，防止同个canal的多个实例监听同一个mysql，导致向同一个canalserver发送多次请求
            switchedAddresses.add(canalAddress);
        }

        return result;
    }

    /**
     * 向某个具体的canal server发送db切换请求
     * 
     * @param canalAddress
     *            canal地址
     * @param data
     *            db切换数据
     * @return 该canal的db切换结果
     */
    private CanalDBSwitchResult doCanalDBSwitch(InetSocketAddress canalAddress, CanalDBSwitchRequestData data) {
        CanalDBSwitchResult result = null;

        try {
            result = CanalConnectorManager.getConnector(canalAddress).canalDBSwitch(data);
        } catch (Exception e) {
            // 向canal发送请求时失败(如，网络问题)，而不是canal本身的切换处理失败(可能请求还未发送到canal)
            String errotMsg = "canal db switch goes wrong.";
            LOG.error(errotMsg, e);
            result = new CanalDBSwitchResult();
            result.setResultCode(CanalDBSwitchResult.RESULT_CODE_FAILED);
            result.setResultMsg(errotMsg + e.getMessage());
        }

        alarmCanalDBSwitchFailed(canalAddress, data, result); // 接入报警
        return result;
    }

    /**
     * 切换失败报警，针对某个canal server的切换失败报警
     * 
     * @param result
     */
    private void alarmCanalDBSwitchFailed(InetSocketAddress canalAddress, CanalDBSwitchRequestData data, CanalDBSwitchResult result) {
        if (result.getResultCode() != CanalDBSwitchResult.RESULT_CODE_SUCCESS) {
            String requestJson = data.toJson();
            LOG.error("canal server db switch failed, canalAddress:{}; request:{}; errorMsg:{}", //
                    canalAddress, requestJson, result.getResultMsg());
            if (!CollectionUtils.isEmpty(result.getInstanceResults())) {
                for (CanalDBSwitchInstanceResult instanceResult : result.getInstanceResults()) {
                    LOG.error("canal instance db switch failed, canalAddress:{}; destaination:{}, request:{}; errorMsg:{}",//
                            canalAddress, instanceResult.getInstanceIdentity().getDestination(), requestJson, instanceResult.getResultMsg());
                }
            }
        }
    }

    /**
     * 切换失败报警
     * 
     * @param result
     */
    private void alarmCanalDBSwitchFailed(String requestJson, CanalDBSwitchResult result) {
        LOG.error("canal server db switch failed, request:{}; errorMsg:{}", requestJson, result.getResultMsg());
        if (!CollectionUtils.isEmpty(result.getInstanceResults())) {
            for (CanalDBSwitchInstanceResult instanceResult : result.getInstanceResults()) {
                LOG.error("canal instance db switch failed, destaination:{}, request:{}; errorMsg:{}",//
                        instanceResult.getInstanceIdentity().getDestination(), requestJson, instanceResult.getResultMsg());
            }
        }
    }

}
