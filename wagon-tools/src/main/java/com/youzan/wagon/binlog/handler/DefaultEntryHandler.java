package com.youzan.wagon.binlog.handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.google.protobuf.InvalidProtocolBufferException;
import com.youzan.wagon.binlog.RowDataUtils;

/**
 * Entry处理器默认实现：将符合条件的Entry记录到结果集中
 * 
 * @author wangguofeng since 2016年5月6日 下午9:53:39
 */
public class DefaultEntryHandler implements EntryHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String schemaName;
    private String tableName;
    private Map<String, String> columns = new HashMap<String, String>();
    private List<RowResult> results = new ArrayList<RowResult>();

    public DefaultEntryHandler() {
    }

    public void check() {
        if (StringUtils.isBlank(tableName)) {
            throw new CanalParseException("tableName is null.");
        }
        if (columns.size() == 0) {
            throw new CanalParseException("columns is empty.");
        }
    }

    @Override
    public void handle(Entry entry) throws CanalParseException, InvalidProtocolBufferException {
        // 事务开始或结尾标识
        if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
            return;
        }

        // 库名不匹配(如果schemaName属性不为空)
        CanalEntry.Header header = entry.getHeader();
        if (StringUtils.isNotBlank(schemaName) && !schemaName.equalsIgnoreCase(header.getSchemaName())) {
            return;
        }

        // 表名不匹配
        if (!tableName.equalsIgnoreCase(header.getTableName())) {
            return;
        }

        RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
        List<RowData> rowDataList = rowChange.getRowDatasList();
        for (RowData rowData : rowDataList) { // 该事务事件所有行
            if (matched(rowData)) { // 如果该行匹配所有列条件
                EntryPosition position = new EntryPosition(header.getLogfileName(), header.getLogfileOffset(), header.getExecuteTime());
                RowResult result = new RowResult(rowChange.getEventType(), position, rowData);
                result.setSchemaName(header.getSchemaName());
                results.add(result);
            }
        }
    }

    public void addColumn(String columnName, String columnValue) {
        columns.put(columnName, columnValue);
    }

    // ================== help method ===================

    /**
     * 判断该行是否匹配查询条件
     * 
     * @param row
     * @return
     */
    private boolean matched(RowData rowData) {
        Map<String, Column> columnMap = RowDataUtils.toColumnMap(rowData.getAfterColumnsList());

        for (Map.Entry<String, String> columnCondition : columns.entrySet()) {
            String columnName = columnCondition.getKey();
            if (!columnMap.containsKey(columnName)) {
                return false; // 不存在该条件字段
            }

            if (!matched(columnMap.get(columnName).getValue(), columnCondition.getValue())) {
                return false; // 其中一个不满足就不匹配
            }
        }

        return true;
    }

    /**
     * 判断某列值是否满足某条具体字段规则
     * 
     * @param row
     * @return
     */
    private boolean matched(String columnValue, String conditionValue) {
        return StringUtils.isBlank(columnValue) ? StringUtils.isBlank(conditionValue) : columnValue.equals(conditionValue);
    }

    /**
     * 符合查询条件的行数据，
     */
    public static class RowResult {
        private String schemaName;
        private String tableName;

        private String eventType;
        private EntryPosition position;
        private RowData rowData;
        private String dateFormat;

        public RowResult() {
        }

        public RowResult(EventType eventType, EntryPosition position, RowData rowData) {
            this.eventType = eventType.name();
            this.position = position;
            this.rowData = rowData;

            this.dateFormat = DATE_FORMAT.format(new Date(this.position.getTimestamp()));
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public EntryPosition getPosition() {
            return position;
        }

        public void setPosition(EntryPosition position) {
            this.position = position;
        }

        public RowData getRowData() {
            return rowData;
        }

        public void setRowData(RowData rowData) {
            this.rowData = rowData;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

    }

    // ================== setter / getter ===================

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, String> columns) {
        this.columns = columns;
    }

    public List<RowResult> getResults() {
        return results;
    }

    public void setResults(List<RowResult> results) {
        this.results = results;
    }

}
