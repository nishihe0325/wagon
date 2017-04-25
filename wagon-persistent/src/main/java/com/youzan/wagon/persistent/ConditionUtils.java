package com.youzan.wagon.persistent;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author wangguofeng since 2016年4月25日 下午1:51:54
 */
public class ConditionUtils {

    public static final String ID = "id";

    public static final String CANAL_HOST_NAME = "canalHostName";
    public static final String CANAL_HOST = "canalHost";
    public static final String CANAL_PORT = "canalPort";
    public static final String DESTINATION = "destination";

    public static final String CLIENT_ID = "clientId";
    public static final String DB_HOST = "dbHost";
    public static final String DB_PORT = "dbPort";

    public static final String LIMIT_NUM = "limitNum";

    // rule
    public static final String RULE_ID = "ruleId";
    public static final String BIZ_NAME = "bizName";
    public static final String TABLE_NAME = "tableName";
    public static final String TOPIC_NAME = "topicName";

    public static final void put(Map<String, Object> condition, String key, String value) {
        if (condition != null && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            condition.put(key, value);
        }
    }

    public static final void put(Map<String, Object> condition, String key, Long value) {
        if (condition != null && StringUtils.isNotBlank(key) && value != null) {
            condition.put(key, value);
        }
    }

    public static final void put(Map<String, Object> condition, String key, Integer value) {
        if (condition != null && StringUtils.isNotBlank(key) && value != null) {
            condition.put(key, value);
        }
    }

    public static final void put(Map<String, Object> condition, String key, Short value) {
        if (condition != null && StringUtils.isNotBlank(key) && value != null) {
            condition.put(key, value);
        }
    }
}
