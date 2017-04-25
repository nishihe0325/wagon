package com.youzan.wagon.persistent.model;

import java.util.Date;

/**
 * 实例信息，包括实例所属的canal server信息；实例名信息；实例连接的mysql信息；
 * 
 * @author wangguofeng since 2016年1月6日 下午3:24:07
 */
public class CanalInstanceInfo {

    private Long id;

    // 所属的canal server信息
    private String canalHostName;
    private String canalHost;
    private Integer canalPort;

    // 实例
    private String destination;
    private Long slaveId;

    // 监听的mysql信息
    private String dbHost;
    private Integer dbPort;
    private String dbUsername;

    private Date createTime;
    private Date modifyTime;

    public CanalInstanceInfo() {
    }

    public CanalInstanceInfo(String canalHost, Integer canalPort, String destination) {
        this.canalHost = canalHost;
        this.canalPort = canalPort;
        this.destination = destination;
    }

    // ================== setter / getter ===================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCanalHostName() {
        return canalHostName;
    }

    public void setCanalHostName(String canalHostName) {
        this.canalHostName = canalHostName;
    }

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

    public Long getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Long slaveId) {
        this.slaveId = slaveId;
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
