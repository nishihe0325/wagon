package com.youzan.wagon.persistent.provide;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.youzan.wagon.common.WagonConstants;

/**
 * @author wangguofeng since 2016年6月3日 下午4:17:21
 */
public class RuleInfoSqlProvider {
    private static final String ALL_COLMUNS = "biz_name, table_name, topic_name, event_type, enable_state, push_data_type, field_condition_type, out_type, rule_owner, organization, create_time, modify_time";

    public String findByMapSql(Map<String, Object> condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, " + ALL_COLMUNS + " FROM rule_info Where 1");

        // where条件(考虑如何优化)
        if (condition != null && !condition.isEmpty()) {
            String bizName = (String) condition.get(WagonConstants.KEY_BIZ_NAME);
            String tableName = (String) condition.get(WagonConstants.KEY_TABLE_NAME);
            String topicName = (String) condition.get(WagonConstants.KEY_TOPIC_NAME);

            if (StringUtils.isNotBlank(bizName)) {
                sql.append(" AND biz_name like '%" + bizName + "%'");
            }
            if (StringUtils.isNotBlank(tableName)) {
                sql.append(" AND table_name like '%" + tableName + "%'");
            }
            if (StringUtils.isNotBlank(topicName)) {
                sql.append(" AND topic_name like '%" + topicName + "%'");
            }
        }

        return sql.toString();
    }

}
