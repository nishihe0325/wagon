package com.alibaba.otter.canal.common.protocol;

import com.alibaba.otter.canal.common.protocol.RemotingSerializable;

import java.util.HashMap;
import java.util.Map;


/**
 * canal从shiva获取某个server包含的实例信息的结果
 *
 * @author wangguofeng since 2015年12月31日 上午11:48:46
 */
public class GetInstanceInfoResult extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1; // 获取成功
    public static final int RESULT_CODE_FAILED = -1; // 获取失败

    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "successful";

    private String canalHost;
    private Integer canalPort;
    private Map<String /* destination */, CanalInstanceData> instanceDatas = new HashMap<String, CanalInstanceData>();

    public GetInstanceInfoResult() {
    }

    public GetInstanceInfoResult(String canalHost, Integer canalPort) {
        this.canalHost = canalHost;
        this.canalPort = canalPort;
    }

    public boolean isNotEmpty() {
        return instanceDatas != null && instanceDatas.size() > 0;
    }

    public void addInstanceData(CanalInstanceData data) {
        instanceDatas.put(data.getDestination(), data);
    }

    public CanalInstanceData getInstanceData(String destination) {
        return instanceDatas.get(destination);
    }

    // ================== setter / getter ===================

    public String getCanalHost() {
        return canalHost;
    }

    public void setCanalHost(String canalHost) {
        this.canalHost = canalHost;
    }

    public Integer getCanalPort() {
        return canalPort;
    }

    public void setCanalPort(Integer canalPort) {
        this.canalPort = canalPort;
    }

    public Map<String, CanalInstanceData> getInstanceDatas() {
        return instanceDatas;
    }

    public void setInstanceDatas(Map<String, CanalInstanceData> instanceDatas) {
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

}
