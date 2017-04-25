package com.youzan.wagon.persistent.model;

import java.util.Date;

public class RuleBizInfo {
    private Long id;
    private String version;
    private String bizName;
    private Date createTime;
    private Date modifyTime;

    // ==================以下属性只作为请求参数使用==========
    private int pageSize;

    // ======================================

    public RuleBizInfo() {
    }

    public RuleBizInfo(String bizName) {
        this.bizName = bizName;
    }

    // ====================== get and set ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
