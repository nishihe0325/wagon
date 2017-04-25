package com.alibaba.otter.canal.common.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 所有canal实例数据集合,即,实例名和CanalInstanceData对象的映射Map。
 * 包含了系统启动时(或者实例配置监控时),从console获取最新的所有实例配置信息集合,
 * 也可以在将正在运行的实例配置和运行位点信息同步给控制台时,作为数据包,包含当前运行的所有实例的信息。
 * <p>
 * Created by wangguofeng on 17/3/14.
 */
public class CanalInstanceWrapperData extends RemotingSerializable {

    private String version;
    private String serviceName;

    private String host;
    private Integer port;

    private Map<String, CanalInstanceDataV2> instanceDatas = new HashMap<String, CanalInstanceDataV2>();

    // ================== constructor ===================
    public CanalInstanceWrapperData() {
    }

    public CanalInstanceWrapperData(String serviceName, String version) {
        this.serviceName = serviceName;
        this.version = version;
    }

    public CanalInstanceWrapperData(String serviceName, String host, Integer port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }


    // ================== operator ===================
    public void addInstanceData(CanalInstanceDataV2 canalInstanceData) {
        instanceDatas.put(canalInstanceData.getInstanceName(), canalInstanceData);
    }

    // ================== setter / getter ===================
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, CanalInstanceDataV2> getInstanceDatas() {
        return instanceDatas;
    }

    public void setInstanceDatas(Map<String, CanalInstanceDataV2> instanceDatas) {
        this.instanceDatas = instanceDatas;
    }

}
