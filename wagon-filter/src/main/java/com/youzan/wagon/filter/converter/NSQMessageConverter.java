
package com.youzan.wagon.filter.converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.rule.LogicRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.common.util.ProcessorUtils;
import com.youzan.wagon.filter.bean.RowDataBean;
import com.youzan.wagon.filter.exception.RuleFilterException;
import com.youzan.wagon.filter.utils.TypeConverter;

public class NSQMessageConverter implements MessageConverter {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    /**
     * 转化为需要被推送到消息队列的消息
     *
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    @Override
    public String conver(RowDataBean bean, TopicRule topicRule, EventType eventType, CanalEntry.Header header) throws Exception {
        EventType type = topicRule.getEventType();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Column> beforeColumns = bean.getBeforeColumnsList();
        List<Column> afterColumns = bean.getAfterColumnsList();

        if (type == EventType.INSERT || type == EventType.UPDATE) {
            Map<String, Object> after = outFieldProcess(afterColumns, topicRule);
            outMapLogicProcess(after, topicRule, bean.getAfterColumnsMap());
            if (topicRule.isPushBeforeData()) {
                Map<String, Object> before = outFieldProcess(beforeColumns, topicRule);
                map = pushAllData(before, after);
            } else {
                map = after;
            }
        } else if (type == EventType.DELETE) {
            Map<String, Object> before = outFieldProcess(beforeColumns, topicRule);
            outMapLogicProcess(before, topicRule, bean.getBeforeColumnsMap());
            if (topicRule.isPushBeforeData()) {
                Map<String, Object> after = outFieldProcess(afterColumns, topicRule);
                map = pushAllData(before, after);
            } else {
                map = before;
            }
        }

        // 添加其他属性，并转化为json
        map.put("add_mq_time", System.currentTimeMillis());
        map.put("eventType", eventType.name());
        map.put("schemaName", header.getSchemaName());
        map.put("binlogFileName", header.getLogfileName());
        map.put("binlogOffset", header.getLogfileOffset());
        map.put("binlogTime", header.getExecuteTime());
        map.put("binlogTableName", header.getTableName());
        map.put("binlogSequenceNo", header.getBinlogSequenceNo());
        map.put("binlogTransactionNo", header.getTransactionNo());
        return JSON.toJSONString(map, SerializerFeature.BrowserCompatible);
    }

    // ============================ help method ===========================
    private Map<String, Object> outFieldProcess(List<Column> columns, TopicRule topicRule) {
        if (CollectionUtils.isEmpty(columns)) {
            return new HashMap<String, Object>();
        }
        Map<String, Object> map = ProcessorUtils.feedMap(columns);
        if (topicRule == null || CollectionUtils.isEmpty(topicRule.getOutFieldList())) {
            return map;
        }

        if (topicRule.isOutFieldIgnore()) {
            for (String ignoreField : topicRule.getOutFieldList()) {
                map.remove(ignoreField);
            }
            return map;
        } else {
            Map<String, Object> newMap = new HashMap<String, Object>();

            // 所有有变更的字段需要输出
            for (Column column : columns) {
                if (column.getUpdated()) {
                    newMap.put(column.getName(), map.get(column.getName()));
                }
            }

            // 手动添加的字段需要输出
            for (String ignoreField : topicRule.getOutFieldList()) {
                Object value = map.get(ignoreField);
                if (value != null) {
                    newMap.put(ignoreField, value);
                }
            }

            return newMap;
        }
    }

    private Map<String, Object> pushAllData(Map<String, Object> beforeMap, Map<String, Object> afterMap) {
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put("before", beforeMap);
        newMap.put("after", afterMap);
        return newMap;
    }

    private void outMapLogicProcess(Map<String, Object> outMap, TopicRule topicRule, Map<String, Column> columnMap) {
        if (CollectionUtils.isNotEmpty(topicRule.getLogicRuleList())) {
            for (LogicRule logicRule : topicRule.getLogicRuleList()) {
                String columnName = logicRule.getName();
                Column column = columnMap.get(columnName);
                String cValue = (String) outMap.get(columnName);
                String rValue = logicRule.getValue();

                if (WagonConstants.RULE_LOGIC_OPERATOR_NEW.equals(logicRule.getOperator())) {
                    String javaType = logicRule.getDataType();
                    outMap.put(columnName, fixedValue(rValue, javaType));
                } else if (WagonConstants.RULE_LOGIC_OPERATOR_FIX.equals(logicRule.getOperator())) {
                    String javaType = TypeConverter.mysql2JavaType(column.getMysqlType());
                    outMap.put(columnName, fixedValue(rValue, javaType));
                } else if (WagonConstants.RULE_LOGIC_OPERATOR_PLUS.equals(logicRule.getOperator())) {
                    if (StringUtils.isBlank(cValue) || StringUtils.isBlank(rValue)) {
                        LOG_ERROR.warn("column or rule logic value is blank,  columnValue=[{}], logicValue=[{}], columnName=[{}]", cValue, rValue, columnName);
                        continue;
                    }
                    String javaType = TypeConverter.mysql2JavaType(column.getMysqlType());
                    outMap.put(columnName, add(cValue, rValue, javaType));
                } else if (WagonConstants.RULE_LOGIC_OPERATOR_SUBTRACT.equals(logicRule.getOperator())) {
                    if (StringUtils.isBlank(cValue) || StringUtils.isBlank(rValue)) {
                        LOG_ERROR.warn("column or rule logic value is blank,  columnValue=[{}], logicValue=[{}], columnName=[{}]", cValue, rValue, columnName);
                        continue;
                    }
                    String javaType = TypeConverter.mysql2JavaType(column.getMysqlType());
                    outMap.put(columnName, decrement(cValue, rValue, javaType));
                }
            }
        }
    }

    private Object fixedValue(String rValue, String javaType) {
        if (TypeConverter.INTEGER.equals(javaType)) {
            return Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(javaType)) {
            return Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(javaType)) {
            return Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(javaType)) {
            return Double.parseDouble(rValue);
        } else {
            throw new RuleFilterException(String.format("The data type %s is not supported for the %s operation.", javaType, "fixedValue"));
        }
    }

    private Object add(String cValue, String rValue, String javaType) {
        if (TypeConverter.INTEGER.equals(javaType)) {
            return Integer.parseInt(cValue) + Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(javaType)) {
            return Long.parseLong(cValue) + Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(javaType)) {
            return Float.parseFloat(cValue) + Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(javaType)) {
            return Double.parseDouble(cValue) + Double.parseDouble(rValue);
        } else {
            throw new RuleFilterException(String.format("The data type %s is not supported for the %s operation.", javaType, "add"));
        }
    }

    private Object decrement(String cValue, String rValue, String javaType) {
        if (TypeConverter.INTEGER.equals(javaType)) {
            return Integer.parseInt(cValue) - Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(javaType)) {
            return Long.parseLong(cValue) - Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(javaType)) {
            return Float.parseFloat(cValue) - Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(javaType)) {
            return Double.parseDouble(cValue) - Double.parseDouble(rValue);
        } else {
            throw new RuleFilterException(String.format("The data type %s is not supported for the %s operation.", javaType, "decrement"));
        }
    }

}
