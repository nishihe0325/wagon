package com.alibaba.otter.canal.common.protocol;

/**
 * canal实例数据,只简单的包含监听哪个数据库实例,从哪里开始dump(或者前已dump到哪里),
 * 可以用作系统启动时(或者实例配置监控时),从console获取最新的实例配置信息时的实例配置数据单元,
 * 也可以在将正在运行的实例配置和运行位点信息同步给控制台时,作为实例运行数据单元。
 * 如果需要包含其他数据时(如是否是master,是否运行中等),可在外等再做一层封装。
 * <p>
 * Created by wangguofeng on 17/3/14.
 */
public class CanalInstanceDataV2 extends RemotingSerializable {
    // 实例名称
    private String instanceName;

    // 监听的mysql信息
    private long slaveId = 1234;
    private String dbHost;
    private int dbPort;
    private String dbUsername;
    private String dbPassword;

    // 客户端消费位置信息
    private String binlogFile;
    private Long binlogOffset;
    private Long binlogExeTime;

    // ================== constructor ===================
    public CanalInstanceDataV2() {
    }

    public CanalInstanceDataV2(String instanceName, String dbHost, int dbPort) {
        this.instanceName = instanceName;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
    }

    public CanalInstanceDataV2(String instanceName, String dbHost, int dbPort, String binlogFile, Long binlogOffset, Long binlogExeTime) {
        this(instanceName, dbHost, dbPort);
        this.binlogFile = binlogFile;
        this.binlogOffset = binlogOffset;
        this.binlogExeTime = binlogExeTime;
    }

    // ================== setter / getter ===================
    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public long getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(long slaveId) {
        this.slaveId = slaveId;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
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

}