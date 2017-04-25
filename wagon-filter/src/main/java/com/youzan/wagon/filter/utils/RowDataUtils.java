package com.youzan.wagon.filter.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;

public class RowDataUtils {

    public static Map<String, Column> toColumnMap(List<Column> columns) {
        Map<String, Column> map = new HashMap<String, Column>();
        if (columns != null) {
            for (Column column : columns) {
                map.put(column.getName(), column);
            }
        }
        return map;
    }

}
