package com.youzan.wagon.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;

public class ProcessorUtils {
    private static final Logger log = LoggerFactory.getLogger(ProcessorUtils.class);

    public static Map<String, Object> feedMap(List<Column> columns) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Column column : columns) {
            map.put(column.getName(), column.getValue());
        }
        return map;
    }

    public static void feedObject(Column column, Set<String> excludeColumn, Object obj) {
        String name = column.getName();
        if (excludeColumn == null || !excludeColumn.contains(name)) {
            feedObject(name, column.getValue(), obj);
        }
    }

    public static void feedObject(String name, String value, Object obj) {
        try {
            Class<?> clazz = obj.getClass();
            Field[] allFields = clazz.getDeclaredFields();
            Map<String, Class<?>> fieldMap = new HashMap<String, Class<?>>(allFields.length);
            for (Field field : allFields) {
                fieldMap.put(field.getName(), field.getType());
            }

            if (fieldMap.containsKey(name)) {
                Class<?> type = fieldMap.get(name);
                Method method = clazz.getMethod(setMethod(name), type);
                method.invoke(obj, str2Obj(value, type));
            }
        } catch (Exception e) {
            log.error("##ERROR ProcessorUtils.feedObject goes wrong at {}\n", ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * 目前就支持 string long integer三种类型
     * 
     * @param value
     * @param type
     * @return
     */
    private static Object str2Obj(String value, Class<?> type) {
        String typeName = type.getSimpleName().toLowerCase();
        switch (typeName) {
            case "string":
                return value;
            case "long":
                return Long.valueOf(value);
            case "integer":
                return Integer.valueOf(value);
            default:
                break;
        }
        return value;
    }

    private static String setMethod(String name) {
        return new StringBuilder("set").append(StringUtils.capitalize(name)).toString();
    }

    private static String getMethod(String name) {
        return new StringBuilder("get").append(StringUtils.capitalize(name)).toString();
    }

    public static boolean objIsEmpty(Object obj) {
        try {
            Class<?> clazz = obj.getClass();
            Field[] allFields = clazz.getDeclaredFields();
            for (Field field : allFields) {
                Method method = clazz.getMethod(getMethod(field.getName()));
                Object value = method.invoke(obj);
                if (value != null)
                    return false;
            }
        } catch (Exception e) {
            log.error("##ERROR ProcessorUtils.objIsEmpty goes wrong at {}\n", ExceptionUtils.getFullStackTrace(e));
        }
        return true;
    }

    public static Set<String> parseTableName(String subscribe) {
        if (StringUtils.isBlank(subscribe))
            return null;
        Set<String> tableNames = new HashSet<String>();
        String[] tables = subscribe.split(",");
        for (String table : tables) {
            String[] tableArr = table.split("\\.");
            int len = tableArr.length;
            if (len == 1)
                tableNames.add(tableArr[0].trim());
            if (len == 2)
                tableNames.add(tableArr[1].trim());
        }

        return tableNames;
    }
}
