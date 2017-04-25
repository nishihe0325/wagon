package com.alibaba.otter.canal.common.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 该canal所有客户端的消费位置信息
 *
 * @author wangguofeng since 2016年3月4日 上午11:35:44
 */
public class CanalClientPositionData extends RemotingSerializable {

    // 对应的canal实例信息
    private String canalHostName;
    private String canalHost;
    private Integer canalPort;
    // 客户端消费位置
    private Map<String /* destination */, List<ClientPositionData>> positionDatas = new HashMap<String /* destination */, List<ClientPositionData>>();

    public CanalClientPositionData() {
    }

    public CanalClientPositionData(String canalHostName, String canalHost, Integer canalPort) {
        this();
        this.canalHostName = canalHostName;
        this.canalHost = canalHost;
        this.canalPort = canalPort;
    }

    public void addPositionData(String destination, ClientPositionData positionData) {
        List<ClientPositionData> positions = positionDatas.get(destination);
        if (positions == null) {
            positions = new ArrayList<ClientPositionData>();
            positionDatas.put(destination, positions);
        }
        positions.add(positionData);
    }

    // ================== inner class ===================

    public static class ClientPositionData {

        // 客户端信息
        private short clientId;

        // 客户端消费位置信息
        private String journalName;
        private Long position;
        private Long timestamp;

        public ClientPositionData() {
        }

        public ClientPositionData(short clientId) {
            this.clientId = clientId;
        }

        public ClientPositionData(short clientId, String journalName, Long position) {
            this.clientId = clientId;
            this.journalName = journalName;
            this.position = position;
        }

        public ClientPositionData(short clientId, String journalName, Long position, Long timestamp) {
            this.clientId = clientId;
            this.journalName = journalName;
            this.position = position;
            this.timestamp = timestamp;
        }

        public short getClientId() {
            return clientId;
        }

        public void setClientId(short clientId) {
            this.clientId = clientId;
        }

        public String getJournalName() {
            return journalName;
        }

        public void setJournalName(String journalName) {
            this.journalName = journalName;
        }

        public Long getPosition() {
            return position;
        }

        public void setPosition(Long position) {
            this.position = position;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

    }

    // ================== setter / getter ===================

    public String getCanalHost() {
        return canalHost;
    }

    public String getCanalHostName() {
        return canalHostName;
    }

    public void setCanalHostName(String canalHostName) {
        this.canalHostName = canalHostName;
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

    public Map<String, List<ClientPositionData>> getPositionDatas() {
        return positionDatas;
    }

    public void setPositionDatas(Map<String, List<ClientPositionData>> positionDatas) {
        this.positionDatas = positionDatas;
    }

}
