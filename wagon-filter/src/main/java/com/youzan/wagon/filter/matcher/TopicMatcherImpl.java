package com.youzan.wagon.filter.matcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.youzan.wagon.common.WagonConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.youzan.wagon.common.rule.FieldRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.common.util.ProcessorUtils;
import com.youzan.wagon.filter.bean.RowDataBean;
import com.youzan.wagon.filter.exception.RuleFilterException;
import com.youzan.wagon.filter.utils.FilterConfigUtil;
import com.youzan.wagon.filter.utils.TypeConverter;

public class TopicMatcherImpl implements TopicMatcher {

    private static final Logger LOG_DEGUG = LoggerFactory.getLogger(WagonConstants.LOG_NAME_DEBUG);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean isMatched(TopicRule topicRule, RowDataBean bean) {
        try {
            List<FieldRule> fieldRuleList = topicRule.getFieldRuleList();
            if (fieldRuleList == null || fieldRuleList.size() == 0) {
                return true; // 没有字段规则限制，则符合该规则
            }

            // 分别对or或and做判断
            if ("and".equals(topicRule.getFieldRuleRela())) {
                return isMatchedAll(topicRule, bean);
            } else {
                return isMatchedOne(topicRule, bean);
            }
        } catch (Exception e) {
            LOG_ERROR.error("matched failed, topic: {}, cause by: {}, columns: {}", topicRule.getTopicName(), ExceptionUtils.getFullStackTrace(e), logColumns(bean, topicRule));
            return false;
        }
    }

    /**
     * 判断该RowData是否符合所有配置的FieldRule，只要不符合其中一条，则返回false；
     * 如果配置的field不存在(即配置的fieldName在表中不存在该列名，一般是配置错误，或表结构改动)，也返回false
     * 
     * @param topicRule
     * @param bean
     * @return
     */
    public boolean isMatchedAll(TopicRule topicRule, RowDataBean bean) {
        EventType eventType = topicRule.getEventType();
        List<FieldRule> fieldRuleList = topicRule.getFieldRuleList();
        boolean needDebug = FilterConfigUtil.needDebug(topicRule.getTableName());

        for (FieldRule fieldRule : fieldRuleList) {
            try {
                Column column = getColumn(fieldRule, bean, eventType);
                if (column == null) {
                    if (needDebug) {
                        LOG_DEGUG.debug("Not matched, column [{}] not exist, topic: {}, columns: {}", fieldRule.getName(), topicRule.getTopicName(), logColumns(bean, topicRule));
                    }
                    return false; // 字段为空，直接return
                }

                if (!isMatched(column, fieldRule, eventType)) {
                    if (needDebug) {
                        LOG_DEGUG.debug("Not matched, column [{}] not matched, topic: {}, columns: {}", fieldRule.getName(), topicRule.getTopicName(), logColumns(bean, topicRule));
                    }
                    return false; // 只要有一个不匹配直接return
                }
            } catch (Exception e) {
                LOG_ERROR.error("Not matched, column [{}] match failed, topic: {}, cause by: {}, columns: {}", fieldRule.getName(), topicRule.getTopicName(), ExceptionUtils.getFullStackTrace(e), logColumns(bean, topicRule));
                return false; // 只要有一个异常直接return
            }
        }

        // 代码执行到这，表明，所有规则都符合
        return true;
    }

    /**
     * 判断该RowData是否符合所有配置的FieldRule，只要其中一条符合，就返回true；
     * 如果配置的field不存在(即配置的fieldName在表中不存在该列名，一般是配置错误，或表结构改动)，则跳过
     * 
     * @param topicRule
     * @param bean
     * @return
     */
    public boolean isMatchedOne(TopicRule topicRule, RowDataBean bean) {
        EventType eventType = topicRule.getEventType();
        List<FieldRule> fieldRuleList = topicRule.getFieldRuleList();
        boolean needDebug = FilterConfigUtil.needDebug(topicRule.getTableName());
        StringBuilder debugInfo = new StringBuilder();

        for (FieldRule fieldRule : fieldRuleList) {
            try {
                Column column = getColumn(fieldRule, bean, eventType); // 找到字段规则名对应的Column对象
                if (column == null) {
                    if (needDebug) {
                        debugInfo.append(String.format("Column [%s] not exist; ", fieldRule.getName()));
                    }
                    continue;
                }

                if (isMatched(column, fieldRule, eventType)) {
                    return true; // 只要有一个匹配直接返回true
                }

                if (needDebug) {
                    debugInfo.append(String.format("Column [%s] not matched; ", fieldRule.getName()));
                }
            } catch (Exception e) { // 其中一个规则异常，只打印
                String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
                if (needDebug) {
                    debugInfo.append(String.format("Column [%s] matched failed, cause by: %s; ", fieldRule.getName(), fullStackTrace));
                }
                LOG_ERROR.error("Column [{}] matched failed, topic: {}, cause by: {}, columns:{}", fieldRule.getName(), topicRule.getTopicName(), fullStackTrace, logColumns(bean, topicRule));
            }
        }

        // 代码执行到这，表明，所有规则都不符合
        if (needDebug) {
            LOG_DEGUG.debug("Not matched, topicName:{}, cause by:{}, columns:{}", topicRule.getTopicName(), debugInfo.toString(), logColumns(bean, topicRule));
        }
        return false;
    }

    /**
     * 获取需要和某个规则进行操作的Column对象，该Column对象的列名和fieldRule的字段名相同，在update事件中，
     * 该对象可能取自更新后的列，也可能取自更新前的列，insert取更新后的列，delete取更新前的列
     * 
     * @param fieldRule
     * @param bean
     * @return
     */
    private Column getColumn(FieldRule fieldRule, RowDataBean bean, EventType eventType) {
        if (eventType == EventType.INSERT) {
            return bean.getAfterColumnsMap().get(fieldRule.getName().trim());
        } else if (eventType == EventType.DELETE) {
            return bean.getBeforeColumnsMap().get(fieldRule.getName().trim());
        } else if (eventType == EventType.UPDATE) {
            if ("before".equals(fieldRule.getBeforeOrAfter())) {
                return bean.getBeforeColumnsMap().get(fieldRule.getName().trim());
            } else {
                return bean.getAfterColumnsMap().get(fieldRule.getName().trim());
            }
        } else {
            throw new RuleFilterException(String.format("The event type %s is not supported.", eventType));
        }
    }

    /**
     * 判断某列是否匹配其对应的字段规则
     * 
     * @param column
     *            :具体某列
     * @param rule
     *            对应的字段规则
     * @return 是否匹配
     * @throws ParseException
     */
    private boolean isMatched(Column column, FieldRule rule, EventType eventType) throws ParseException {
        // 必须有更新(只针对update事件和变更后数据)
        if (eventType == EventType.UPDATE && "after".equals(rule.getBeforeOrAfter())//
                && rule.isMustUpdated() && !column.getUpdated()) {
            return false;
        }

        // 只有"notEmpty"，"empty"，"updated"操作允许字段为空
        String cValue = column.getValue();
        String rValue = rule.getValue();
        String operator = rule.getOperator();
        if ("empty".equals(operator)) {
            return StringUtils.isBlank(cValue);
        } else if ("notEmpty".equals(operator)) {
            return StringUtils.isNotBlank(cValue);
        } else if ("updated".equals(operator)) {
            return column.getUpdated();
        }

        // 其他操作，只要column或tule的值为空，则认为不符合规则
        if (StringUtils.isBlank(cValue) || StringUtils.isBlank(rValue)) {
            return false;
        }

        // 分别对每种操作类型做处理
        String type = TypeConverter.mysql2JavaType(column.getMysqlType());
        if ("=".equals(operator)) {
            return equal(cValue, rValue, type);
        } else if ("!=".equals(operator)) {
            return notEqual(cValue, rValue, type);
        } else if (">".equals(operator)) {
            return greater(cValue, rValue, type);
        } else if (">=".equals(operator)) {
            return eGreater(cValue, rValue, type);
        } else if ("<".equals(operator)) {
            return less(cValue, rValue, type);
        } else if ("<=".equals(operator)) {
            return eLess(cValue, rValue, type);
        } else if ("in".equals(operator)) {
            return in(cValue, rValue, type);
        } else if ("module".equals(operator)) {
            return module(cValue, rValue, type);
        } else {
            throw new RuleFilterException(String.format("The operation %s is not supported.", operator));
        }
    }

    private String logColumns(RowDataBean bean, TopicRule topicRule) {
        EventType type = topicRule.getEventType();
        Map<String, Object> map = new HashMap<String, Object>();

        if (type == EventType.INSERT) {
            map = ProcessorUtils.feedMap(bean.getAfterColumnsList());
        } else if (type == EventType.UPDATE) {
            map.put("before", ProcessorUtils.feedMap(bean.getBeforeColumnsList()));
            map.put("after", ProcessorUtils.feedMap(bean.getAfterColumnsList()));
        } else if (type == EventType.DELETE) {
            map = ProcessorUtils.feedMap(bean.getBeforeColumnsList());
        }

        return JSON.toJSONString(map, SerializerFeature.BrowserCompatible);
    }

    /************************************* operator *******************************************************/

    private static final String OPERATION_NOT_SUPPORTED = "The data type %s is not supported for the %s operation.";

    private boolean equal(String cValue, String rValue, String type) throws ParseException {
        if (TypeConverter.STRING.equals(type)) {
            return cValue.equals(rValue);
        } else if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) == Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) == Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) == Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) == Double.parseDouble(rValue);
        } else if (TypeConverter.DATE.equals(type)) {
            Date cDate = DATE_FORMAT.parse(cValue);
            Date rDate = DATE_FORMAT.parse(rValue);
            return cDate.equals(rDate);
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, "="));
        }
    }

    private boolean notEqual(String cValue, String rValue, String type) throws ParseException {
        if (TypeConverter.STRING.equals(type)) {
            return !cValue.equals(rValue);
        } else if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) != Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) != Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) != Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) != Double.parseDouble(rValue);
        } else if (TypeConverter.DATE.equals(type)) {
            Date cDate = DATE_FORMAT.parse(cValue);
            Date rDate = DATE_FORMAT.parse(rValue);
            return !cDate.equals(rDate);
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, "!="));
        }
    }

    private boolean greater(String cValue, String rValue, String type) throws ParseException {
        if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) > Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) > Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) > Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) > Double.parseDouble(rValue);
        } else if (TypeConverter.DATE.equals(type)) {
            Date cDate = DATE_FORMAT.parse(cValue);
            Date rDate = DATE_FORMAT.parse(rValue);
            return cDate.before(rDate);
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, ">"));
        }
    }

    private boolean eGreater(String cValue, String rValue, String type) throws ParseException {
        if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) >= Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) >= Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) >= Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) >= Double.parseDouble(rValue);
        } else if (TypeConverter.DATE.equals(type)) {
            Date cDate = DATE_FORMAT.parse(cValue);
            Date rDate = DATE_FORMAT.parse(rValue);
            return cDate.before(rDate) || cDate.equals(rDate);
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, ">="));
        }
    }

    private boolean less(String cValue, String rValue, String type) throws ParseException {
        if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) < Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) < Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) < Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) < Double.parseDouble(rValue);
        } else if (TypeConverter.DATE.equals(type)) {
            Date cDate = DATE_FORMAT.parse(cValue);
            Date rDate = DATE_FORMAT.parse(rValue);
            return cDate.after(rDate);
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, "<"));
        }
    }

    private boolean eLess(String cValue, String rValue, String type) throws ParseException {
        if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) <= Integer.parseInt(rValue);
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) <= Long.parseLong(rValue);
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) <= Float.parseFloat(rValue);
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) <= Double.parseDouble(rValue);
        } else if (TypeConverter.DATE.equals(type)) {
            Date cDate = DATE_FORMAT.parse(cValue);
            Date rDate = DATE_FORMAT.parse(rValue);
            return cDate.after(rDate) || cDate.equals(rDate);
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, "<="));
        }
    }

    private boolean in(String cValue, String ruleValue, String type) throws ParseException {
        String[] ruleValues = ruleValue.trim().split(",");
        for (String rValue : ruleValues) {
            if (equal(cValue, rValue, type)) {
                return true; // 只要in中的一个值符合，则返回true
            }
        }
        return false;
    }

    private boolean module(String cValue, String rValue, String type) {
        // 检测规则参数的合法性
        String[] rValueArray = rValue.split(",");
        if (rValueArray.length != 2) {
            throw new RuleFilterException(String.format("illegal rule value: [%s], operation: module.", rValue));
        }
        int muduleTarget = Integer.valueOf(rValueArray[0]);
        int muduledValue = Integer.valueOf(rValueArray[1]);

        // 取模操作
        if (TypeConverter.INTEGER.equals(type)) {
            return Integer.parseInt(cValue) % muduleTarget == muduledValue;
        } else if (TypeConverter.LONG.equals(type)) {
            return Long.parseLong(cValue) % muduleTarget == muduledValue;
        } else if (TypeConverter.FLOAT.equals(type)) {
            return Float.parseFloat(cValue) % muduleTarget == muduledValue;
        } else if (TypeConverter.DOUBLE.equals(type)) {
            return Double.parseDouble(cValue) % muduleTarget == muduledValue;
        } else {
            throw new RuleFilterException(String.format(OPERATION_NOT_SUPPORTED, type, "module"));
        }
    }

}
