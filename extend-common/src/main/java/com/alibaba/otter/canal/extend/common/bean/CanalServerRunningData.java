package com.alibaba.otter.canal.extend.common.bean;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

/**
 * canal server运行信息，包括每个instance的运行信息
 * 
 * @author wangguofeng since 2016年1月24日 上午12:43:02
 */
public class CanalServerRunningData extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAILED = -1;

    // 标识信息
    private String canalHostName;
    private String canalHost;
    private int canalPort;

    // 处理结果
    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "ok";

    private Map<String/* destination */, CanalInstanceRunningData> instanceDatas = new HashMap<String, CanalInstanceRunningData>();

    public CanalServerRunningData() {
    }

    public CanalServerRunningData(String canalHost, String canalHostName, int canalPort) {
        this.canalHost = canalHost;
        this.canalHostName = canalHostName;
        this.canalPort = canalPort;
    }

    public void add(CanalInstanceRunningData instanceData) {
        this.instanceDatas.put(instanceData.getDestination(), instanceData);
    }

    // ================== setter / getter ===================

    public String getCanalHost() {
        return canalHost;
    }

    public void setCanalHost(String canalHost) {
        this.canalHost = canalHost;
    }

    public String getCanalHostName() {
        return canalHostName;
    }

    public void setCanalHostName(String canalHostName) {
        this.canalHostName = canalHostName;
    }

    public Map<String, CanalInstanceRunningData> getInstanceDatas() {
        return instanceDatas;
    }

    public void setInstanceDatas(Map<String, CanalInstanceRunningData> instanceDatas) {
        this.instanceDatas = instanceDatas;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public int getCanalPort() {
        return canalPort;
    }

    public void setCanalPort(int canalPort) {
        this.canalPort = canalPort;
    }

}
