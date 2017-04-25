package com.youzan.wagon.persistent.mvc.service.impl;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.extend.common.bean.CanalInstanceRunningData;
import com.alibaba.otter.canal.extend.common.bean.CanalServerRunningData;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.youzan.wagon.persistent.CanalConnectorManager;
import com.youzan.wagon.persistent.model.CanalRunningInfoWrapper;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.mvc.service.CanalRunningStatusMvcService;
import com.youzan.wagon.persistent.service.CanalServerInfoService;

@Component("canalRunningInfoService")
public class CanalRunningStatusMvcServiceImpl implements CanalRunningStatusMvcService {

    private static final Logger LOG = LoggerFactory.getLogger(CanalRunningStatusMvcServiceImpl.class);

    @Resource()
    private CanalServerInfoService canalServerInfoService;

    // ================== request ===================
    public CanalRunningInfoWrapper canalRunningInfoList(Map<String, Object> condition) {
        CanalRunningInfoWrapper result = new CanalRunningInfoWrapper();
        String destination = "";
        if (condition != null) {
            String value = (String) condition.get("destination");
            if (StringUtils.isNotBlank(value)) {
                destination = value;
            }
        }

        try {
            // 查询出所有符合条件的canal server信息
            List<CanalServerInfo> allCanalServers = canalServerInfoService.findByMap(condition);
            if (!CollectionUtils.isEmpty(allCanalServers)) {
                // 向所有server发送查询请求
                for (CanalServerInfo server : allCanalServers) {
                    CanalServerRunningData data = getCanalServerRunningData(server.getCanalHost(), server.getCanalHostName(), server.getCanalPort(), destination);
                    result.add(fliter(data, condition));// 过滤一下
                }
            }
        } catch (Exception e) {
            LOG.error("get canal running info failed, cause by: {}\n ", ExceptionUtils.getFullStackTrace(e));
            result.setResultCode(CanalRunningInfoWrapper.RESULT_CODE_FAILED);
            result.setResultMsg(e.getMessage());
        }

        return result;
    }

    public CanalInstanceRunningData canalRunningInfoDetail(String canalHost, String canalHostName, //
            int canalPort, String destination) {
        CanalServerRunningData serverData = getCanalServerRunningData(canalHost, canalHostName, canalPort, destination);
        if (serverData != null && serverData.getInstanceDatas().size() > 0) {
            for (CanalInstanceRunningData instanceData : serverData.getInstanceDatas().values()) {
                return instanceData;// 获取第一个(正常情况下只有一个)
            }
        }

        // 执行到这，没有获取到数据
        LOG.error("no instance running data, canalHost:{}, canalHostName:{}, canalPort:{}, times={}", canalHost, canalHostName, canalPort);
        CanalInstanceRunningData data = new CanalInstanceRunningData();
        data.setResultCode(CanalInstanceRunningData.RESULT_CODE_FAILED);
        data.setResultMsg(serverData == null ? "no instance running data matched the query condition." : serverData.getResultMsg());
        return data;
    }

    public String getMonitorData() {
        return canalRunningInfoList(null).toJson();
    }

    // ================== help method ===================

    /**
     * 向指定的canal server发请求，获取该server上指定实例的运行信息(destination为空，则获取所有实例运行信息)
     * 
     * @param canalHost
     *            canal server主机地址
     * @param canalHostName
     *            canal server主机名称
     * @param canalPort
     *            canal server主机端口
     * @param condition
     *            查询条件
     * @return
     */
    private CanalServerRunningData getCanalServerRunningData(String canalHost, String canalHostName, //
            int canalPort, String destination) {
        if (StringUtils.isBlank(destination)) {
            destination = "";// 如果为null，会报错
        }

        InetSocketAddress canalAddr = new InetSocketAddress(canalHost, canalPort);
        String resultMsg = "";
        int retryTimes = 1; // 暂时重试1次
        for (int times = 1; times <= retryTimes; times++) {
            try {
                CanalConnector connector = CanalConnectorManager.getConnector(canalAddr);
                CanalServerRunningData runningData = connector.queryCanalRunningInfo(destination);
                afterRunningData(runningData); // 处理下
                return runningData;
            } catch (CanalClientException e) {
                // 删除连接，删除后，getConnector会自动重连
                CanalConnectorManager.remove(canalAddr);
                resultMsg = e.getMessage();
            } catch (Exception e) {
                resultMsg = e.getMessage();
                LOG.error("getCanalServerRunningData failed, canalHost:{}, canalHostName:{}, canalPort:{}, times={}", canalHost, canalHostName, canalPort, times);
            }
        }

        // 代码执行到这，说明没有获取成功
        CanalServerRunningData result = new CanalServerRunningData(canalHost, canalHostName, canalPort);
        result.setResultCode(CanalServerRunningData.RESULT_CODE_FAILED);
        result.setResultMsg(resultMsg);
        return result;
    }

    /**
     * 手工过滤出符合查询条件的数据
     * 
     * @param runningData
     * @param condition
     * @return
     */
    private CanalServerRunningData fliter(CanalServerRunningData runningData, Map<String, Object> condition) {
        return runningData;
    }

    private CanalServerRunningData afterRunningData(CanalServerRunningData runningData) {
        return runningData;
    }

}
