package com.youzan.wagon.common.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.youzan.wagon.common.rule.TableRule;
import com.youzan.wagon.common.rule.TopicRule;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BizRuleWrap {

    private String bizName;
    private List<TableRule> tableRuleList = new ArrayList<TableRule>();

    public BizRuleWrap() {
    }

    public BizRuleWrap(String bizName) {
        this();
        this.bizName = bizName;
    }

    // =================== get methods ======================================
    public TableRule get(String tableName) {
        for (TableRule tableRule : tableRuleList) {
            if (tableRule.getTableName().equals(tableName)) {
                return tableRule;
            }
        }
        return null;
    }

    /**
     * 获取排序后的列表
     *
     * @return
     */
    public List<TableRule> sortedTableRuleList() {
        Collections.sort(tableRuleList);
        return tableRuleList;
    }

    public TopicRule getTopicRule(String topicRuleId) {
        for (TableRule tableRule : tableRuleList) {
            TopicRule topicRule = tableRule.get(topicRuleId);
            if (topicRule != null) {
                return topicRule;
            }
        }
        return null;
    }

    /**
     * 并排序
     *
     * @return
     */
    public List<String> getAllTableName() {
        List<String> names = new ArrayList<String>();
        for (TableRule tableRule : tableRuleList) {
            names.add(tableRule.getTableName());
        }
        Collections.sort(names);
        return names;
    }

    // =================== add methods ======================================
    public void add(TopicRule topicRule) {
        String tableName = topicRule.getTableName();
        TableRule tableRule = get(tableName);
        if (tableRule == null) {
            tableRule = new TableRule(tableName);
            tableRuleList.add(tableRule);
        }
        tableRule.add(topicRule);
    }

    public void add(List<TableRule> tableRuleList) {
        this.tableRuleList.addAll(tableRuleList);
    }


    // =================== remove methods ======================================
    public void removeTopicRule(String topicRuleId) {
        for (TableRule tableRule : tableRuleList) {
            TopicRule topicRule = tableRule.get(topicRuleId);
            if (topicRule != null) {
                tableRule.remove(topicRule);
                return;
            }
        }
    }

    public void removeTableRule(String tableName) {
        // 新建一个虚的TableRule对象，然后删除，实际删除的是表名相同的列中的项，为了减少一次对数组的遍历
        tableRuleList.remove(new TableRule(tableName));
    }

    public void clear() {
        tableRuleList.clear();
    }

    // =================== update methods ======================================
    public void replace(TopicRule topicRule) {
        removeTopicRule(topicRule.getId()); // 先删除老的规则
        add(topicRule); // 再添加新的规则
    }

    public void onOffTopicRule(String topicRuleId, boolean on) {
        TopicRule topicRule = getTopicRule(topicRuleId);
        if (topicRule != null) {
            topicRule.setActive(on);
        }
    }

    public void onOffTableRule(String tableName, boolean on) {
        TableRule tableRule = get(tableName);
        if (tableRule != null) {
            tableRule.onOff(on);
        }
    }

    // =================== get and set ======================================
    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public List<TableRule> getTableRuleList() {
        return tableRuleList;
    }

    public void setTableRuleList(List<TableRule> tableRuleList) {
        this.tableRuleList = tableRuleList;
    }

}
