package com.youzan.wagon.binlog.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangguofeng since 2016年5月7日 下午2:20:23
 */
public class FilterConditionConfig {

    private String schemaName;
    private String tableName;
    private Map<String, String> columns = new HashMap<String, String>();

    public void addColumn(String columnName, String columnValue) {
        columns.put(columnName, columnValue);
    }

    public FilterConditionConfig() {
    }

    public FilterConditionConfig(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.schemaName = tableName;
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

}
