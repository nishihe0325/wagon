package com.youzan.wagon.common;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;
import com.youzan.wagon.common.rule.TableRule;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleDataWrapper extends RemotingSerializable {

    private String bizName;
    private Map<String/* table name */, TableRule> data = new HashMap<String, TableRule>();

    public RuleDataWrapper() {
    }

    public RuleDataWrapper(String bizName) {
        this.bizName = bizName;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public Map<String, TableRule> getData() {
        return data;
    }

    public void setData(Map<String, TableRule> data) {
        this.data = data;
    }

}
