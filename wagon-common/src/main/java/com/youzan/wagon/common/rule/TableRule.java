package com.youzan.wagon.common.rule;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wangguofeng
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableRule implements Comparable<TableRule> {

    private String tableName;
    private List<TopicRule> topicRuleList = new ArrayList<TopicRule>();
    private boolean active = true;

    public TableRule() {
    }

    public TableRule(String tableName) {
        this();
        this.tableName = tableName;
    }

    public void add(TopicRule topicRule) {
        topicRuleList.add(topicRule);
    }

    /**
     * 获取排序后的列表
     *
     * @return
     */
    public List<TopicRule> sortedTopicRuleList() {
        Collections.sort(topicRuleList);
        return topicRuleList;
    }

    public void remove(TopicRule topicRule) {
        topicRuleList.remove(topicRule);
    }

    public TopicRule get(String topicRuleId) {
        for (TopicRule topicRule : topicRuleList) {
            if (topicRule.getId().equals(topicRuleId)) {
                return topicRule;
            }
        }
        return null;
    }

    public int size() {
        return topicRuleList.size();
    }

    public void onOff(boolean on) {
        setActive(on);
        // 暂停或激活属于该表的所有规则
        for (TopicRule topicRule : topicRuleList) {
            topicRule.setActive(on);
        }
    }

    // ========================= get and set =========================
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

    public List<TopicRule> getTopicRuleList() {
        return topicRuleList;
    }

    public void setTopicRuleList(List<TopicRule> topicRuleList) {
        this.topicRuleList = topicRuleList;
    }

    /************************************************************************************************/
    /**
     * 表名相同，即认为相等
     **/
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof TableRule) {
            TableRule other = (TableRule) obj;
            return (other.getTableName()).equals(this.getTableName());
        }
        return false;
    }

    /**
     * 根据表名称排序
     *
     * @param arg0
     * @return
     */
    @Override
    public int compareTo(TableRule arg0) {
        return this.getTableName().compareTo(arg0.getTableName());
    }

}
