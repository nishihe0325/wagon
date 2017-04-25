package com.youzan.wagon.persistent.model;

import java.util.Date;

import com.youzan.wagon.common.WagonConstants;

public class RuleInfo {
    private Long id;

    private String bizName;
    private String tableName;
    private String schemaName = ""; // 库名,允许为空,为空则说明不指定数据库,默认空字符串
    private String topicName;
    private String eventType; // INSERT，UPDATE，DELETE

    private String ruleOwner;
    private String organization;

    private Short enableState = WagonConstants.RULE_ENABLE_STATE_ENABLED; // 1，启用；2，禁用；
    private Short pushDataType = WagonConstants.RULE_PUSH_DATA_TYPE_ALL; // 1，更新前后同时推送;2，只推送更新后，或前(delete)
    private Short fieldConditionType = WagonConstants.RULE_FIELD_CONDITION_AND; // 1，and;2，or
    private Short outType = WagonConstants.RULE_OUT_TYPE_IGNORE; // 1，只忽略，2，只输出

    private Integer maxRetryCount; // 最大重试次数,如果为空,则无限重试知道成功
    private String seqConsumeField; // mq顺序消费字段,如果为空,则无需顺序

    private Date createTime;
    private Date modifyTime;

    // ==================以下属性只作为请求参数使用==========
    private int pageSize;

    // ======================================

    public RuleInfo() {
    }

    public RuleInfo(String bizName, String tableName, String topicName, String eventType) {
        this.bizName = bizName;
        this.tableName = tableName;
        this.topicName = topicName;
        this.eventType = eventType;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getRuleOwner() {
        return ruleOwner;
    }

    public void setRuleOwner(String ruleOwner) {
        this.ruleOwner = ruleOwner;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Short getEnableState() {
        return enableState;
    }

    public void setEnableState(Short enableState) {
        this.enableState = enableState;
    }

    public Short getPushDataType() {
        return pushDataType;
    }

    public void setPushDataType(Short pushDataType) {
        this.pushDataType = pushDataType;
    }

    public Short getFieldConditionType() {
        return fieldConditionType;
    }

    public void setFieldConditionType(Short fieldConditionType) {
        this.fieldConditionType = fieldConditionType;
    }

    public Short getOutType() {
        return outType;
    }

    public void setOutType(Short outType) {
        this.outType = outType;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getSeqConsumeField() {
        return seqConsumeField;
    }

    public void setSeqConsumeField(String seqConsumeField) {
        this.seqConsumeField = seqConsumeField;
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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

}
