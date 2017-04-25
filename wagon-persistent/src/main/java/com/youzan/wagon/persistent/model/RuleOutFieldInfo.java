package com.youzan.wagon.persistent.model;

import java.util.Date;

public class RuleOutFieldInfo {
    private Long id;
    private Long ruleId;

    private String bizName;
    private String tableName;
    private String topicName;

    private String fieldName;

    private Date createTime;
    private Date modifyTime;

    // ==================以下属性只作为请求参数使用==========
    private String addOrEditType;

    // ======================================

    public RuleOutFieldInfo() {
    }

    public RuleOutFieldInfo(Long ruleId, String bizName, String tableName, String topicName, String fieldName) {
        this.ruleId = ruleId;
        this.bizName = bizName;
        this.tableName = tableName;
        this.topicName = topicName;
        this.fieldName = fieldName;
    }

    // ====================== get and set ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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

    public String getAddOrEditType() {
        return addOrEditType;
    }

    public void setAddOrEditType(String addOrEditType) {
        this.addOrEditType = addOrEditType;
    }

}
