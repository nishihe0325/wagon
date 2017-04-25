package com.youzan.wagon.persistent.model;

import java.util.Date;

public class RuleLogicFieldInfo {
    private Long id;
    private Long ruleId;

    private String bizName;
    private String tableName;
    private String topicName;

    private String fieldName;
    private String dataType;
    private String fieldOperator;
    private String fieldValue;

    private Date createTime;
    private Date modifyTime;

    // ==================以下属性只作为请求参数使用==========
    private String addOrEditType;

    // ======================================

    public RuleLogicFieldInfo() {
    }

    public RuleLogicFieldInfo(long ruleId, String bizName, String tableName, String topicName) {
        this.ruleId = ruleId;
        this.bizName = bizName;
        this.tableName = tableName;
        this.topicName = topicName;
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

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getFieldOperator() {
        return fieldOperator;
    }

    public void setFieldOperator(String fieldOperator) {
        this.fieldOperator = fieldOperator;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
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
