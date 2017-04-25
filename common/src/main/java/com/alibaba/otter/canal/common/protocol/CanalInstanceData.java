package com.alibaba.otter.canal.common.protocol;


/**
 * 用于canal和shiva交互时，描述某个实例的相关信息
 *
 * @author wangguofeng since 2015年12月31日 上午11:48:46
 */
public class CanalInstanceData extends RemotingSerializable {

    // 对应的canal信息
    private String canalHostName;
    private String canalHost;
    private Integer canalPort;

    // 实例信息
    private String destination;
    private Long slaveId;

    // 监听的mysql信息
    private String dbHost;
    private Integer dbPort;
    private String dbUsername;
    private String dbPassword;

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

    public Long getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Long slaveId) {
        this.slaveId = slaveId;
    }

    public String getCanalHostName() {
        return canalHostName;
    }

    public void setCanalHostName(String canalHostName) {
        this.canalHostName = canalHostName;
    }

}
