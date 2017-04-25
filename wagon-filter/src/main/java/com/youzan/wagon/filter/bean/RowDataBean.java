package com.youzan.wagon.filter.bean;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.youzan.wagon.filter.utils.RowDataUtils;

/**
 * 该类用于存放处理RowData过程中，产生的临时数据，主要是为了在各个过程中共享数据，避免重复数据处理，比如将RowDate转化为Map
 * 
 * @author wangguofeng
 *
 */
public class RowDataBean {

    private RowData row;
    private List<Column> beforeColumnsList; // 更新前Column列
    private List<Column> afterColumnsList; // 更新后Column列
    private Map<String, Column> beforeColumnsMap; // 更新前Column Map
    private Map<String, Column> afterColumnsMap; // 更新后Column Map

    public RowDataBean(RowData row) {
        this.row = row;
    }

    public List<Column> getBeforeColumnsList() {
        if (beforeColumnsList == null) {
            beforeColumnsList = row.getBeforeColumnsList();
        }
        return beforeColumnsList;
    }

    public List<Column> getAfterColumnsList() {
        if (afterColumnsList == null) {
            afterColumnsList = row.getAfterColumnsList();
        }
        return afterColumnsList;
    }

    public Map<String, Column> getBeforeColumnsMap() {
        if (beforeColumnsMap == null) {
            beforeColumnsMap = RowDataUtils.toColumnMap(getBeforeColumnsList());
        }
        return beforeColumnsMap;
    }

    public Map<String, Column> getAfterColumnsMap() {
        if (afterColumnsMap == null) {
            afterColumnsMap = RowDataUtils.toColumnMap(getAfterColumnsList());
        }
        return afterColumnsMap;
    }

}
