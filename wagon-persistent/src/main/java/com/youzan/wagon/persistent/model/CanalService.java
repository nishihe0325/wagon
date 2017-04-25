package com.youzan.wagon.persistent.model;

import java.util.Date;

public class CanalService {
    private Long id;

    private String serviceName;
    private String ruleBizName;

    private Date createTime;
    private Date modifyTime;

    public CanalService() {
    }

    public CanalService(String serviceName, String ruleBizName) {
        this.serviceName = serviceName;
        this.ruleBizName = ruleBizName;
    }

    // ================== setter / getter ===================
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
