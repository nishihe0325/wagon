package com.youzan.wagon.common.rule;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldRule {

    private String name; // 表字段名，规则操作对象
    private String operator; // 规则操作，目前只支持：=，!=，<，<=，>，>=，in，notEmpty，empty
    private String value; // 规则操作值，当operator为empty和notEmpty时，该值不会被使用

    // ================= 以下属性只在update事件中使用 =================
    private String beforeOrAfter = "after"; // 规则操作作用在该字段更新前，或更新后(默认为更新后，比如，更新前某个字段值是否满足规则)
    private boolean mustUpdated = true; // 是否必须有更新，如果为true，则在update事件中，更新前后，该字段值不一样才符合规则

    public FieldRule() {
    }

    public FieldRule(String name, String operator) {
        this();
        this.name = name;
        this.operator = operator;
    }

    public FieldRule(String name, String operator, String value) {
        this(name, operator);
        this.value = value;
    }

    public FieldRule(String name, String operator, String value, String beforeOrAfter) {
        this(name, operator, value);
        this.beforeOrAfter = beforeOrAfter;
    }

    public FieldRule(String name, String operator, String value, String beforeOrAfter, boolean mustUpdated) {
        this(name, operator, value, beforeOrAfter);
        this.mustUpdated = mustUpdated;
    }

    // ================= set and get =================
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

    public String getBeforeOrAfter() {
        return beforeOrAfter;
    }

    public void setBeforeOrAfter(String beforeOrAfter) {
        this.beforeOrAfter = beforeOrAfter;
    }

    public boolean isMustUpdated() {
        return mustUpdated;
    }

    public void setMustUpdated(boolean mustUpdated) {
        this.mustUpdated = mustUpdated;
    }

}
