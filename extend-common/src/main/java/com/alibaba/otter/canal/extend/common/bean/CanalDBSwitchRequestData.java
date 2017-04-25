package com.alibaba.otter.canal.extend.common.bean;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

/**
 * <pre>
 * mysql切换请求信息封装，主要用途：
 * shiva consle在接收到运维发送过来的mysql切换请求消息时(请求消息为json字符串)，将该消息转化为该类的对象，
 * 并在随后的处理，以及和canal的交互中，使用该对象。
 * </pre>
 * 
 * @author wangguofeng since 2015年12月31日 上午11:46:37
 */
public class CanalDBSwitchRequestData extends RemotingSerializable {

    private DBData fromDBData; // 切换前的mysql实例信息
    private DBData toDBData; // 切换后的mysql实例信息

    public CanalDBSwitchRequestData() {
    }

    public CanalDBSwitchRequestData(DBData fromDBData, DBData toDBData) {
        this.fromDBData = fromDBData;
        this.toDBData = toDBData;
    }

    // ================== setter / getter ===================

    public DBData getFromDBData() {
        return fromDBData;
    }

    public void setFromDBData(DBData fromDBData) {
        this.fromDBData = fromDBData;
    }

    public DBData getToDBData() {
        return toDBData;
    }

    public void setToDBData(DBData toDBData) {
        this.toDBData = toDBData;
    }

    public static class DBData {
        private String host;
        private Integer port;
        private String username;
        private String password;

        public DBData() {
        }

        public DBData(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        public DBData(String host, Integer port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
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
    }

}
