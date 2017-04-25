package com.youzan.wagon.persistent.model;

import com.alibaba.otter.canal.common.WagonConstants;

import java.util.Date;

public class CanalServer {
    private Long id;

    //所属的服务名
    private String serviceName;

    //该服务程序所在主机信息
    private String host;
    private String hostName;
    private Integer port;

    //该服务程序的角色和运行状态
    private String role = WagonConstants.CANAL_SERVER_ROLE_MASTER;
    private String serviceState = WagonConstants.CANAL_SERVER_STATE_RUNNING;

    private Date createTime;
    private Date modifyTime;

    public CanalServer() {
    }

    public CanalServer(String serviceName, String host, String hostName, Integer port) {
        this.serviceName = serviceName;
        this.host = host;
        this.hostName = hostName;
        this.port = port;
    }

    // ================== setter / getter ===================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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
