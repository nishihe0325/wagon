package com.alibaba.otter.canal.extend.common.bean;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

/**
 * canal db切换结果，包含一个或多个canal实例的切换结果
 * 
 * @author wangguofeng since 2016年2月1日 下午4:49:15
 */
public class CanalDBSwitchResult extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1; // 全部实例切换成功
    public static final int RESULT_CODE_FAILED = -1; // 没有全部实例切换成功

    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "successful";
    private List<CanalDBSwitchInstanceResult> instanceResults = new ArrayList<CanalDBSwitchInstanceResult>();

    public void add(CanalDBSwitchInstanceResult result) {
        instanceResults.add(result);
    }

    public void add(CanalDBSwitchResult result) {
        if (result.isNotEmpty()) {
            for (CanalDBSwitchInstanceResult instanceResult : result.getInstanceResults()) {
                instanceResults.add(instanceResult);
            }
        }
    }

    public boolean isNotEmpty() {
        return instanceResults != null && instanceResults.size() > 0;
    }

    // ================== setter / getter ===================

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

    public List<CanalDBSwitchInstanceResult> getInstanceResults() {
        return instanceResults;
    }

    public void setInstanceResults(List<CanalDBSwitchInstanceResult> instanceResults) {
        this.instanceResults = instanceResults;
    }

}
