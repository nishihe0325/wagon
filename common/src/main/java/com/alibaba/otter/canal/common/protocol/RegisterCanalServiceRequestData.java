package com.alibaba.otter.canal.common.protocol;


import com.alibaba.otter.canal.common.protocol.RemotingSerializable;

public class RegisterCanalServiceRequestData extends RemotingSerializable {

    private String hostName;
    private String host;
    private int port;

    private String serviceName;
    private String bizName;

    public RegisterCanalServiceRequestData() {
    }

    public RegisterCanalServiceRequestData(String hostName, String host, int port, String serviceName, String bizName) {
        this.hostName = hostName;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.bizName = bizName;
    }

    // ================== setter / getter ===================

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[hostName=").append(hostName);
        buffer.append("; host=").append(host);
        buffer.append("; port=").append(port);
        buffer.append("; serviceName=").append(serviceName);
        buffer.append("; bizName=").append(bizName);
        buffer.append("]");

        return buffer.toString();
    }
}
