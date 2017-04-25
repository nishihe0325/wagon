package com.alibaba.otter.canal.extend.common.bean;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogIdentity;

/**
 * canal实例运行信息，主要包含以下几个方面：
 * 
 * <pre>
 * 位点信息：
 * 对应的mysql binlog文件的最新位点：binlogPosition
 * canal实例dump的最新位点：dumpPosition
 * canal client消费的最新位点(支持多客户端情况下，一个客户端一位点)：consumePositionTable
 * </pre>
 * 
 * <pre>
 * buffer信息： 
 * 总大小：bufferSizeTotal，bufferCapacityTotal
 * 已用大小：bufferSizeUsed，bufferCapacityUsed
 * 剩余大小：bufferSizeRemain，bufferCapacityRemain
 * </pre>
 * 
 * <pre>
 * 消费延迟：
 * 消费延迟(mysql最新位点的执行时间，减canal client最新消费位点的执行时间)：consumeDelayTable
 * </pre>
 * 
 * @author wangguofeng since 2016年1月23日 下午11:23:00
 */
public class CanalInstanceRunningData extends RemotingSerializable {

    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAILED = -1;

    // 标识信息
    private String destination;
    private LogIdentity mysqlPosition;

    // 处理结果
    private int resultCode = RESULT_CODE_SUCCESS;
    private String resultMsg = "ok";

    // 位点信息
    private EntryPosition binlogPosition;
    private EntryPosition dumpPosition;
    private Map<String/* clientId */, EntryPosition> consumePositionTable = new HashMap<String, EntryPosition>();

    // buffer信息(条数)
    private long bufferSizeTotal;
    private long bufferSizeUsed;
    private long bufferSizeRemain;

    // buffer信息(容量)
    private long bufferCapacityTotal;
    private long bufferCapacityUsed;
    private long bufferCapacityRemain;

    // 消费延迟
    private Map<String/* clientId */, Long> consumeDelayTable = new HashMap<String, Long>();

    public CanalInstanceRunningData() {
    }

    public CanalInstanceRunningData(String destination) {
        this.destination = destination;
    }

    // ================== setter / getter ===================
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LogIdentity getMysqlPosition() {
        return mysqlPosition;
    }

    public void setMysqlPosition(LogIdentity mysqlPosition) {
        this.mysqlPosition = mysqlPosition;
    }

    public EntryPosition getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(EntryPosition binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public EntryPosition getDumpPosition() {
        return dumpPosition;
    }

    public void setDumpPosition(EntryPosition dumpPosition) {
        this.dumpPosition = dumpPosition;
    }

    public Map<String, EntryPosition> getConsumePositionTable() {
        return consumePositionTable;
    }

    public void setConsumePositionTable(Map<String, EntryPosition> consumePositionTable) {
        this.consumePositionTable = consumePositionTable;
    }

    public long getBufferSizeTotal() {
        return bufferSizeTotal;
    }

    public void setBufferSizeTotal(long bufferSizeTotal) {
        this.bufferSizeTotal = bufferSizeTotal;
    }

    public long getBufferSizeUsed() {
        return bufferSizeUsed;
    }

    public void setBufferSizeUsed(long bufferSizeUsed) {
        this.bufferSizeUsed = bufferSizeUsed;
    }

    public long getBufferSizeRemain() {
        return bufferSizeRemain;
    }

    public void setBufferSizeRemain(long bufferSizeRemain) {
        this.bufferSizeRemain = bufferSizeRemain;
    }

    public long getBufferCapacityTotal() {
        return bufferCapacityTotal;
    }

    public void setBufferCapacityTotal(long bufferCapacityTotal) {
        this.bufferCapacityTotal = bufferCapacityTotal;
    }

    public long getBufferCapacityUsed() {
        return bufferCapacityUsed;
    }

    public void setBufferCapacityUsed(long bufferCapacityUsed) {
        this.bufferCapacityUsed = bufferCapacityUsed;
    }

    public long getBufferCapacityRemain() {
        return bufferCapacityRemain;
    }

    public void setBufferCapacityRemain(long bufferCapacityRemain) {
        this.bufferCapacityRemain = bufferCapacityRemain;
    }

    public Map<String, Long> getConsumeDelayTable() {
        return consumeDelayTable;
    }

    public void setConsumeDelayTable(Map<String, Long> consumeDelayTable) {
        this.consumeDelayTable = consumeDelayTable;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

}
