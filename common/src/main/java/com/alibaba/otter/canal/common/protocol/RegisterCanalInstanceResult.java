package com.alibaba.otter.canal.common.protocol;


import com.alibaba.otter.canal.common.protocol.RemotingSerializable;

/**
 * canal实例注册结果
 *
 * @author wangguofeng since 2016年1月12日 上午1:48:20
 */
public class RegisterCanalInstanceResult extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAILED = -1;

    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "successful";

    public RegisterCanalInstanceResult() {
    }

    public RegisterCanalInstanceResult(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public RegisterCanalInstanceResult(int resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public static RegisterCanalInstanceResult buildFailedResult(String resultMsg) {
        return new RegisterCanalInstanceResult(RESULT_CODE_FAILED, resultMsg);
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

}
