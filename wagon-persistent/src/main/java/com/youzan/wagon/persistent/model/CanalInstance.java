package com.youzan.wagon.persistent.model;

import com.youzan.wagon.common.WagonConstants;

import java.util.Date;

public class CanalInstance {
    private Long id;

    // 所属服务,实例名,slaveId
    private Long serviceId;
    private String serviceName;
    private String instanceName;
    private Long slaveId = 1234L;

    // mysql信息
    private String dbHostName;
    private String dbHost;
    private Integer dbPort;
    private String dbUsername;
    private String dbPassword;

    // 位点信息
    private String binlogFile;
    private Long binlogOffset;
    private Long binlogExeTime;

    private Date createTime;
    private Date modifyTime;

    public CanalInstance() {
    }

    public CanalInstance(Long serviceId, String serviceName, String instanceName, String dbHost, Integer dbPort) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.instanceName = instanceName;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
    }

    // ================== setter / getter ===================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Long getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Long slaveId) {
        this.slaveId = slaveId;
    }

    public String getDbHostName() {
        return dbHostName;
    }

    public void setDbHostName(String dbHostName) {
        this.dbHostName = dbHostName;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public Integer getDbPort() {
        return dbPort;
    }

    public void setDbPort(Integer dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    public Long getBinlogOffset() {
        return binlogOffset;
    }

    public void setBinlogOffset(Long binlogOffset) {
        this.binlogOffset = binlogOffset;
    }

    public Long getBinlogExeTime() {
        return binlogExeTime;
    }

    public void setBinlogExeTime(Long binlogExeTime) {
        this.binlogExeTime = binlogExeTime;
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

}
