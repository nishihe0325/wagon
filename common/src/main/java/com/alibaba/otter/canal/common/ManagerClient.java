package com.alibaba.otter.canal.common;

import com.alibaba.otter.canal.common.protocol.*;
import com.alibaba.otter.canal.common.utils.HttpClientUtil;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerClient {
    private static final Logger LOG = LoggerFactory.getLogger(ManagerClient.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);

    private String managerUrl;

    public ManagerClient() {
        this.managerUrl = PropertiesManager.get(WagonConstants.PRO_CONSOLE_ADDRESS);
        if (StringUtils.isBlank(managerUrl)) {
            throw new CanalException("the param [console.address] can't be blank.");
        }
        this.managerUrl = StringUtils.removeEnd(managerUrl, "/");
    }

    // ================================ request(new)=======================================
    public CanalServiceData getCanalServiceData(String serviceName) throws Exception {
        String url = managerUrl + WagonConstants.URL_GET_CANAL_SERVICE_DATAS + "?serviceName=" + serviceName;
        String json = HttpClientUtil.httpRequestByGet(url);
        return CanalServiceData.fromJson(json, CanalServiceData.class);
    }

    public CanalInstanceWrapperData getCanalInstanceDatas(String serviceName) throws Exception {
        String url = managerUrl + WagonConstants.URL_GET_CANAL_INSTANCE_DATAS + "?serviceName=" + serviceName;
        String json = HttpClientUtil.httpRequestByGet(url);
        return CanalInstanceWrapperData.fromJson(json, CanalInstanceWrapperData.class);
    }

    public void updateInstanceDatas(CanalInstanceWrapperData wrapperData) throws Exception {
        String url = this.managerUrl + WagonConstants.URL_UPDATE_INSTANCE_DATAS;
        HttpClientUtil.httpRequestByPost(url, wrapperData.toJson());
    }

    // ================================== request(old)======================================
    public GetInstanceInfoResult getCanalInstanceDatas(String canalHost, Integer canalPort, String destination) throws Exception {
        if (StringUtils.isBlank(canalHost) || canalPort == null) {
            throw new CanalException("the param canalHost and canalPort should not be blank.");
        }

        // url
        StringBuilder buffer = new StringBuilder(managerUrl);
        buffer.append(WagonConstants.URL_GET_INSTANCE_INFO);
        buffer.append("?canalHost=").append(canalHost);
        buffer.append("&canalPort=").append(canalPort);
        if (StringUtils.isNotBlank(destination)) {
            buffer.append("&destination=").append(destination);
        }

        // 发送请求，并返回结果
        String json = HttpClientUtil.httpRequestByGet(buffer.toString());
        return StringUtils.isBlank(json) ? null : GetInstanceInfoResult.fromJson(json, GetInstanceInfoResult.class);
    }

    public RegisterCanalInstanceResult registerCanalInstance(CanalInstanceData data) throws Exception {
        String url = this.managerUrl + WagonConstants.URL_REGISTER_INSTANCE;

        // 发送请求，并返回结果
        String json = HttpClientUtil.httpRequestByPost(url, data.toJson());
        return StringUtils.isBlank(json) ? new RegisterCanalInstanceResult(RegisterCanalInstanceResult.RESULT_CODE_FAILED, "register result is blank.")//
                : RegisterCanalInstanceResult.fromJson(json, RegisterCanalInstanceResult.class);
    }

    public void updatePosition(CanalClientPositionData data) throws Exception {
        String url = this.managerUrl + WagonConstants.URL_UPDATE_POSITION;

        // 发送请求，并返回结果
        HttpClientUtil.httpRequestByPost(url, data.toJson());
    }

}
