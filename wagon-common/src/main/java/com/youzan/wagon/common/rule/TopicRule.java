package com.youzan.wagon.common.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicRule implements Comparable<TopicRule> {

    private String id;
    private String tableName;
    private String topicName;
    private EventType eventType;

    private Integer maxRetryCount; // 最大重试次数,如果为空,则无限重试知道成功
    private String seqConsumeField; // mq顺序消费字段,如果为空,则无需顺序

    private boolean outFieldIgnore = false; // 是否忽略outFieldList中的字段，为true，则，输出除了outFieldList的其他字段，如果为false，则只输出outFieldList中的字段
    private boolean active = true;
    private String fieldRuleRela = "and";// 字段规则的关系，即是所有都符合还是其中一个符合即可，默认所有都符合
    private boolean pushBeforeData = false; // 该属性只在update事件中使用

    // 以下属性可选
    private List<FieldRule> fieldRuleList = new ArrayList<FieldRule>();
    private List<String> outFieldList = new ArrayList<String>();
    private List<LogicRule> logicRuleList = new ArrayList<LogicRule>();

    public TopicRule() {
        this.id = UUID.randomUUID().toString();
    }

    public TopicRule(String id) {
        this.id = id;
    }

    public TopicRule(String tableName, String topicName, EventType eventType) {
        this();
        this.tableName = tableName;
        this.topicName = topicName;
        this.eventType = eventType;
    }

    public TopicRule(String id, String tableName, String topicName, EventType eventType) {
        this.id = id;
        this.tableName = tableName;
        this.topicName = topicName;
        this.eventType = eventType;
    }

    public void addFieldRule(FieldRule fieldRule) {
        fieldRuleList.add(fieldRule);
    }

    public void addFieldRule(List<FieldRule> fieldRuleList) {
        if (fieldRuleList != null) {
            for (FieldRule fieldRule : fieldRuleList) {
                addFieldRule(fieldRule);
            }
        }
    }

    public void addoutFieldName(String outFieldName) {
        outFieldList.add(outFieldName);
    }

    public void addLogicRule(LogicRule logicRule) {
        logicRuleList.add(logicRule);
    }

    public void addLogicRule(List<LogicRule> logicRuleList) {
        if (logicRuleList != null) {
            for (LogicRule logicRule : logicRuleList) {
                addLogicRule(logicRule);
            }
        }
    }

    // ================= set and get ==================================
    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<FieldRule> getFieldRuleList() {
        return fieldRuleList;
    }

    public void setFieldRuleList(List<FieldRule> fieldRuleList) {
        this.fieldRuleList = fieldRuleList;
    }

    public List<String> getOutFieldList() {
        return outFieldList;
    }

    public void setOutFieldList(List<String> outFieldList) {
        this.outFieldList = outFieldList;
    }

    public boolean isPushBeforeData() {
        return pushBeforeData;
    }

    public void setPushBeforeData(boolean pushBeforeData) {
        this.pushBeforeData = pushBeforeData;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFieldRuleRela() {
        return fieldRuleRela;
    }

    public void setFieldRuleRela(String fieldRuleRela) {
        this.fieldRuleRela = fieldRuleRela;
    }

    public boolean isOutFieldIgnore() {
        return outFieldIgnore;
    }

    public void setOutFieldIgnore(boolean outFieldIgnore) {
        this.outFieldIgnore = outFieldIgnore;
    }

    public List<LogicRule> getLogicRuleList() {
        return logicRuleList;
    }

    public void setLogicRuleList(List<LogicRule> logicRuleList) {
        this.logicRuleList = logicRuleList;
    }

    /************************************************************************************************/
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof TopicRule) {
            TopicRule other = (TopicRule) obj;
            return (other.getId()).equals(this.getId());
        }
        return false;
    }

    /**
     * @param arg0
     * @return
     */
    @Override
    public int compareTo(TopicRule arg0) {
        return this.getTopicName().compareTo(arg0.getTopicName());
    }

}
