package com.youzan.wagon.common.util;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * @author wangguofeng since 2016年3月30日 下午6:22:46
 */
public class PropertiesUtils {

    public static String getProperty(Properties properties, String key) {
        return StringUtils.trim(properties.getProperty(StringUtils.trim(key)));
    }

    public static String getProperty(Properties properties, String key, String def) {
        String value = getProperty(properties, key);
        return StringUtils.isBlank(value) ? def : value;
    }

    public static Integer getInteger(Properties properties, String key) {
        String value = getProperty(properties, key);
        return StringUtils.isBlank(value) ? null : Integer.valueOf(value);
    }

    public static Integer getInteger(Properties properties, String key, Integer def) {
        String value = getProperty(properties, key);
        return StringUtils.isBlank(value) ? def : Integer.valueOf(value);
    }

    public static Long getLong(Properties properties, String key) {
        String value = getProperty(properties, key);
        return value == null ? null : Long.valueOf(value);
    }

    public static Long getLong(Properties properties, String key, Long def) {
        String value = getProperty(properties, key);
        return StringUtils.isBlank(value) ? def : Long.valueOf(value);
    }

    public static Boolean getBoolean(Properties properties, String key) {
        String value = getProperty(properties, key);
        return value == null ? null : Boolean.valueOf(value);
    }

    public static boolean getBoolean(Properties properties, String key, boolean def) {
        String value = getProperty(properties, key);
        return StringUtils.isBlank(value) ? def : Boolean.valueOf(value);
    }

}
