package com.youzan.wagon.persistent.model;

public class TablePositionBean {

    // 实例地址
    private String canalHost;
    private Integer canalPort;
    private String canalHostName;
    private String destination;

    // db地址
    private String dbAddress;
    private String username;
    private String password;

    // db地址
    private String tableSchema;
    private String tableName;

    // 业务名
    private String bizName;

    // 说明
    private String remark;

    public TablePositionBean() {
    }

    public TablePositionBean(String canalHost, Integer canalPort, String canalHostName, String destination, String bizName, String dbAddress) {
        this.canalHost = canalHost;
        this.canalPort = canalPort;
        this.canalHostName = canalHostName;
        this.destination = destination;
        this.bizName = bizName;
        this.dbAddress = dbAddress;
    }

    public TablePositionBean clone() {
        TablePositionBean bean = new TablePositionBean(this.canalHost, Integer.valueOf(this.canalPort), this.canalHostName, this.destination, this.bizName, this.dbAddress);
        bean.setUsername(this.username);
        bean.setPassword(this.password);
        bean.setTableName(this.tableName);
        bean.setTableSchema(this.tableSchema);
        bean.setRemark(this.remark);
        return bean;
    }

    // ================== setter / getter ===================

    public String getCanalHost() {
        return canalHost;
    }

    public void setCanalHost(String canalHost) {
        this.canalHost = canalHost;
    }

    public Integer getCanalPort() {
        return canalPort;
    }

    public void setCanalPort(Integer canalPort) {
        this.canalPort = canalPort;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public void setDbAddress(String dbAddress) {
        this.dbAddress = dbAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCanalHostName() {
        return canalHostName;
    }

    public void setCanalHostName(String canalHostName) {
        this.canalHostName = canalHostName;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

}
