package com.alibaba.otter.canal.common.protocol;

import java.util.Date;

public class CanalServiceData extends RemotingSerializable {

    private Long id;

    private String serviceName;
    private String ruleBizName;

    private String mqAddress;

    private Date createTime;
    private Date modifyTime;

    // ================== constructor ===================
    public CanalServiceData() {
    }

    public CanalServiceData(String serviceName, String ruleBizName) {
        this.serviceName = serviceName;
        this.ruleBizName = ruleBizName;
    }

    // ==================================================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRuleBizName() {
        return ruleBizName;
    }

    public void setRuleBizName(String ruleBizName) {
        this.ruleBizName = ruleBizName;
    }

    public String getMqAddress() {
        return mqAddress;
    }

    public void setMqAddress(String mqAddress) {
        this.mqAddress = mqAddress;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

}