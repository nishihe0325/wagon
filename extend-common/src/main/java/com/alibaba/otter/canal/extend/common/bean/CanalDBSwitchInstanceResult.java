package com.alibaba.otter.canal.extend.common.bean;

import java.net.InetSocketAddress;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

/**
 * 具体某个canal实例对应的数据库切换结果
 * 
 * @author wangguofeng since 2016年2月1日 上午10:47:36
 */
public class CanalDBSwitchInstanceResult extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1; // 切换成功
    public static final int RESULT_CODE_FAILED = -1; // 切换失败

    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "successful";
    private InstanceIdentity instanceIdentity; // 唯一确定一个canal实例

    public CanalDBSwitchInstanceResult() {
    }

    public CanalDBSwitchInstanceResult(InstanceIdentity instanceIdentity) {
        this.instanceIdentity = instanceIdentity;
    }

    public CanalDBSwitchInstanceResult(InetSocketAddress canalAddress, String destination) {
        this.instanceIdentity = new InstanceIdentity(canalAddress, destination);
    }

    public CanalDBSwitchInstanceResult(String canalHost, Integer canalPost, String destination) {
        this.instanceIdentity = new InstanceIdentity(canalHost, canalPost, destination);
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

    public InstanceIdentity getInstanceIdentity() {
        return instanceIdentity;
    }

    public void setInstanceIdentity(InstanceIdentity instanceIdentity) {
        this.instanceIdentity = instanceIdentity;
    }

    // ================== inner class ===================
    public static class InstanceIdentity {

        private InetSocketAddress canalAddress;
        private String destination;

        public InstanceIdentity() {
        }

        public InstanceIdentity(InetSocketAddress canalAddress, String destination) {
            this.canalAddress = canalAddress;
            this.destination = destination;
        }

        public InstanceIdentity(String canalHost, Integer canalPost, String destination) {
            this.canalAddress = new InetSocketAddress(canalHost, canalPost);
            this.destination = destination;
        }

        public InetSocketAddress getCanalAddress() {
            return canalAddress;
        }

        public void setCanalAddress(InetSocketAddress canalAddress) {
            this.canalAddress = canalAddress;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }
    }

}
