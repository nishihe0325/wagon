package com.youzan.wagon.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.youzan.wagon.common.RuleDataWrapper;
import com.youzan.wagon.common.rule.FieldRule;
import com.youzan.wagon.common.rule.TableRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.persistent.model.RuleFieldInfo;
import com.youzan.wagon.persistent.model.RuleInfo;
import com.youzan.wagon.persistent.model.RuleOutFieldInfo;

/**
 * @author wangguofeng since 2016年4月19日 上午9:30:21
 */
public class RuleBeanUtil {

    /**
     * 将某个业务下的说有规则列表；规则字段列表，规则输出字段列表，组装成RuleDataWrapper对象
     * 
     * @param bizName
     * @param ruleInfos
     * @param fieldInfos
     * @param outFieldInfos
     * @return
     */
    public static RuleDataWrapper buildRuleDataWrapper(String bizName, List<RuleInfo> ruleInfos, List<RuleFieldInfo> fieldInfos, List<RuleOutFieldInfo> outFieldInfos) {
        RuleDataWrapper wrapper = new RuleDataWrapper(bizName);
        if (CollectionUtils.isEmpty(ruleInfos)) {
            return wrapper;
        }

        Map<Long, List<FieldRule>> ruleIdFieldMap = buildRuleFieldTable(fieldInfos);
        Map<Long, List<String>> ruleIdOutFieldMap = buildOutFieldTable(outFieldInfos);

        // 将RuleInfo放入对应的TableRule中
        Map<String/* table name */, TableRule> tableRuleMap = wrapper.getData();
        for (RuleInfo ruleInfo : ruleInfos) {
            TopicRule topicRule = buildTopicRule(ruleInfo);
            TableRule tableRule = tableRuleMap.get(ruleInfo.getTableName());
            // if (tableRule == null) {
            // tableRule = new TableRule(ruleInfo.getTableName());
            // tableRule.setEnableState(TopicRule.ENABLE_STATE_DISABLE);//
            // 先设置为false
            // tableRuleMap.put(ruleInfo.getTableName(), tableRule);
            // }
            // if (topicRule.getEnableState() == TopicRule.ENABLE_STATE_ENABLE)
            // {
            // tableRule.setEnableState(TopicRule.ENABLE_STATE_ENABLE); //
            // 有一条激活，即为激活
            // }
            tableRule.add(topicRule);
            topicRule.setFieldRuleList(ruleIdFieldMap.get(ruleInfo.getId()));
            topicRule.setOutFieldList(ruleIdOutFieldMap.get(ruleInfo.getId()));
        }

        return wrapper;
    }

    /**
     * 获取某条规则，和属于该规则的所有条件字段列表的映射Map
     * 
     * @param fieldInfos
     * @return
     */
    private static Map<Long/* ruldId */, List<FieldRule>> buildRuleFieldTable(List<RuleFieldInfo> fieldInfos) {
        Map<Long, List<FieldRule>> map = new HashMap<Long, List<FieldRule>>();
        if (CollectionUtils.isEmpty(fieldInfos)) {
            return map;
        }

        for (RuleFieldInfo fieldInfo : fieldInfos) {
            List<FieldRule> list = map.get(fieldInfo.getRuleId());
            if (list == null) {
                list = new ArrayList<FieldRule>();
                map.put(fieldInfo.getRuleId(), list);
            }
            list.add(buildFieldRule(fieldInfo));
        }

        return map;
    }

    /**
     * 获取某条规则，和属于该规则的所有输出字段列表的映射Map
     * 
     * @param outFieldInfos
     * @return
     */
    private static Map<Long/* ruldId */, List<String>> buildOutFieldTable(List<RuleOutFieldInfo> outFieldInfos) {
        Map<Long, List<String>> map = new HashMap<Long, List<String>>();
        if (CollectionUtils.isEmpty(outFieldInfos)) {
            return map;
        }

        for (RuleOutFieldInfo outFieldInfo : outFieldInfos) {
            List<String> list = map.get(outFieldInfo.getRuleId());
            if (list == null) {
                list = new ArrayList<String>();
                map.put(outFieldInfo.getRuleId(), list);
            }
            list.add(outFieldInfo.getFieldName());
        }

        return map;
    }

    private static FieldRule buildFieldRule(RuleFieldInfo info) {
        FieldRule rieldRule = new FieldRule();
        // rieldRule.setFieldName(info.getFieldName());
        // rieldRule.setFieldOperator(info.getFieldOperator());
        // rieldRule.setFieldValue(info.getFieldValue());

        // rieldRule.setPositionType(info.getPositionType());
        rieldRule.setMustUpdated(info.isMustUpdated());
        return rieldRule;
    }

    private static TopicRule buildTopicRule(RuleInfo info) {
        TopicRule topicRule = new TopicRule();
        topicRule.setTableName(info.getTableName());
        topicRule.setTopicName(info.getTopicName());

        // topicRule.setEventType(info.getEventType());
        // topicRule.setEnableState(info.getEnableState());
        //
        // topicRule.setPushDataType(info.getPushDataType());
        // topicRule.setFieldConditionType(info.getFieldConditionType());
        // topicRule.setOutType(info.getOutType());
        return topicRule;
    }
}
