package com.alibaba.otter.canal.extend.common.bean;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

/**
 * 表所在mysql地址信息，canal地址等信息
 * 
 * @author wangguofeng since 2016年3月3日 上午10:16:52
 */
public class TablePositionData extends RemotingSerializable {

    // 对应的canal地址
    private String canalHost;
    private Integer canalPort;
    private String tableName; // 表名(简单表名或全表名:库名+表名)
    // 该canal下包含该表的mysql实例地址
    private Map<String /* destination */, InetSocketAddress> mysqlAddresses = new HashMap<String, InetSocketAddress>();

    public TablePositionData() {
    }

    public TablePositionData(String canalHost, Integer canalPort, String tableName) {
        this.canalHost = canalHost;
        this.canalPort = canalPort;
        this.tableName = tableName;
    }

    public void addAddress(String destination, InetSocketAddress mysqlAddress) {
        mysqlAddresses.put(destination, mysqlAddress);
    }

    public void addAddress(String destination, String mysqlHost, int mysqlPort) {
        mysqlAddresses.put(destination, new InetSocketAddress(mysqlHost, mysqlPort));
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

    public Map<String, InetSocketAddress> getMysqlAddresses() {
        return mysqlAddresses;
    }

    public void setMysqlAddresses(Map<String, InetSocketAddress> mysqlAddresses) {
        this.mysqlAddresses = mysqlAddresses;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
