package com.youzan.wagon.persistent.provide;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.youzan.wagon.persistent.ConditionUtils;

/**
 * @author wangguofeng since 2016年4月29日 上午9:27:45
 */
public class CanalClientPositionInfoProvide {

    public String getByMap(Map<String, Object> condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM canal_client_position_info");
        sql.append(" Where 1=1");

        // where条件
        if (condition != null && !condition.isEmpty()) {
            String id = (String) condition.get(ConditionUtils.ID);
            String canalHostName = (String) condition.get(ConditionUtils.CANAL_HOST_NAME);
            String canalHost = (String) condition.get(ConditionUtils.CANAL_HOST);
            Integer canalPort = (Integer) condition.get(ConditionUtils.CANAL_PORT);
            String destination = (String) condition.get(ConditionUtils.DESTINATION);
            Short clientId = (Short) condition.get(ConditionUtils.CLIENT_ID);

            if (StringUtils.isNotBlank(id)) {
                sql.append(String.format(" AND id=%d", id));
            }
            if (StringUtils.isNotBlank(canalHostName)) {
                sql.append(String.format(" AND canal_host_name='%s'", canalHostName));
            }
            if (StringUtils.isNotBlank(canalHost)) {
                sql.append(String.format(" AND canal_host='%s'", canalHost));
            }
            if (canalPort != null) {
                sql.append(String.format(" AND canal_port=%d", canalPort));
            }
            if (StringUtils.isNotBlank(destination)) {
                sql.append(String.format(" AND canal_port='%s'", destination));
            }
            if (clientId != null) {
                sql.append(String.format(" AND canal_port=%d", clientId));
            }

        }

        return sql.toString();
    }

}
