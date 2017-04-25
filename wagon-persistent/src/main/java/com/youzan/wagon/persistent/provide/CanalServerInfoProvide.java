package com.youzan.wagon.persistent.provide;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.youzan.wagon.persistent.ConditionUtils;

/**
 * @author wangguofeng since 2016年4月29日 上午9:27:45
 */
public class CanalServerInfoProvide {

    public String getByMap(Map<String, Object> condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT canal_host_name, canal_host, canal_port FROM canal_instance_info");
        sql.append(" Where 1=1");

        // where条件
        if (condition != null && !condition.isEmpty()) {
            String id = (String) condition.get(ConditionUtils.ID);
            String canalHostName = (String) condition.get(ConditionUtils.CANAL_HOST_NAME);
            String canalHost = (String) condition.get(ConditionUtils.CANAL_HOST);
            Integer canalPort = (Integer) condition.get(ConditionUtils.CANAL_PORT);

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
        }

        sql.append(" GROUP BY canal_host_name, canal_host, canal_port");
        sql.append(" ORDER BY canal_host_name, canal_host, canal_port");

        return sql.toString();
    }

}
