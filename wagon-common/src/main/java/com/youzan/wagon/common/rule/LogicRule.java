package com.youzan.wagon.common.rule;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicRule {
    private String name;
    private String operator;
    private String value;
    private String dataType; // 数据类型,当操作类型(operator值)是添加一个在原来数据库中不存在的字段时使用

    public LogicRule() {
    }

    public LogicRule(String name, String operator) {
        this();
        this.name = name;
        this.operator = operator;
    }

    public LogicRule(String name, String operator, String value) {
        this(name, operator);
        this.value = value;
    }

    public LogicRule(String name, String operator, String value, String beforeOrAfter) {
        this(name, operator, value);
    }

    //================== set and get ==================
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

}
