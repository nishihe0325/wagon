package com.youzan.wagon.persistent.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;
import com.alibaba.otter.canal.extend.common.bean.CanalServerRunningData;

/**
 * canal运行信息封装，包含所有注册到wagon console的canal运行信息
 * 
 * @author wangguofeng since 2016年4月22日 下午4:32:21
 */
public class CanalRunningInfoWrapper extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAILED = -1;

    // 处理结果
    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "ok";

    // key为canal server地址的字符串形式，如："127.0.0.1：111111"
    private Map<String, CanalServerRunningData> serverDatas = new HashMap<String, CanalServerRunningData>();

    public CanalRunningInfoWrapper() {
    }

    public void add(CanalServerRunningData data) {
        if (data != null) {
            String key = data.getCanalHost();
            if (StringUtils.isBlank(key)) {
                key = data.getCanalHostName();
            }
            key = key + ":" + data.getCanalPort();

            this.serverDatas.put(key, data);
        }
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

    public Map<String, CanalServerRunningData> getServerDatas() {
        return serverDatas;
    }

    public void setServerDatas(Map<String, CanalServerRunningData> serverDatas) {
        this.serverDatas = serverDatas;
    }

}
