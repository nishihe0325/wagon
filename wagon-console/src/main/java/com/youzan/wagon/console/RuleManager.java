package com.youzan.wagon.console;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.WagonException;
import com.youzan.wagon.common.rule.FieldRule;
import com.youzan.wagon.common.rule.LogicRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.common.rule.BizRuleWrap;
import com.youzan.wagon.persistent.model.RuleBizInfo;
import com.youzan.wagon.persistent.model.RuleFieldInfo;
import com.youzan.wagon.persistent.model.RuleInfo;
import com.youzan.wagon.persistent.model.RuleLogicFieldInfo;
import com.youzan.wagon.persistent.model.RuleOutFieldInfo;
import com.youzan.wagon.persistent.service.RuleBizInfoService;
import com.youzan.wagon.persistent.service.RuleFieldInfoService;
import com.youzan.wagon.persistent.service.RuleInfoService;
import com.youzan.wagon.persistent.service.RuleLogicFieldInfoService;
import com.youzan.wagon.persistent.service.RuleOutFieldInfoService;

@Component("ruleManager")
public class RuleManager {
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private Map<String, BizRuleWrapInner> bizMap = new HashMap<String, BizRuleWrapInner>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // ========================== @Resource =================================
    @Resource()
    private RuleBizInfoService ruleBizInfoService;
    @Resource()
    private RuleInfoService ruleInfoService;
    @Resource()
    private RuleFieldInfoService ruleFieldInfoService;
    @Resource()
    private RuleOutFieldInfoService ruleOutFieldInfoService;
    @Resource()
    private RuleLogicFieldInfoService ruleLogicFieldInfoService;

    // ==========================================================================

    /**
     * 任何异常，往上抛(上层处理)。没有该业务对应的规则，则返回空json串
     *
     * @param bizName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getRuleJson(String bizName) throws IOException, InterruptedException {
        BizRuleWrap wrap = null;
        try {
            this.lock.readLock().lockInterruptibly();
            BizRuleWrapInner bizRuleWrapInner = bizMap.get(bizName);
            if (bizRuleWrapInner != null) {
                wrap = bizRuleWrapInner.wrap;
            } else {
                LOG_HEARTBEAT.warn("there has no rule info for bizName:[{}]", bizName);
            }
        } finally {
            this.lock.readLock().unlock();
        }

        wrap = wrap != null ? wrap : new BizRuleWrap(bizName); // 为空，则返回空json字符串
        return new ObjectMapper().writeValueAsString(wrap.getTableRuleList());
    }

    public String getAllRuleJson() throws IOException, InterruptedException {
        try {
            this.lock.readLock().lockInterruptibly();
            return JSON.toJSONString(bizMap, SerializerFeature.BrowserCompatible);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void update() throws IOException, InterruptedException {
        List<RuleBizInfo> allBizs = ruleBizInfoService.findAll(); // 查询出所有业务信息

        Map<String, RuleBizInfo> allBizMap = new HashMap<String, RuleBizInfo>();
        if (CollectionUtils.isNotEmpty(allBizs)) {
            for (RuleBizInfo biz : allBizs) {
                allBizMap.put(biz.getBizName(), biz);
            }
        }

        if (isNeedUpdate(allBizMap)) {
            // 查询所有的规则，及规则字段，输出字段，逻辑字段
            List<RuleInfo> ruleInfos = ruleInfoService.findAll();
            if (CollectionUtils.isEmpty(ruleInfos)) {
                LOG_ERROR.warn("ruleInfos is empty.");
                return; // 是否直接返回的处理策略还需再考虑
            }
            List<RuleFieldInfo> ruleFields = ruleFieldInfoService.findAll();
            List<RuleOutFieldInfo> outFields = ruleOutFieldInfoService.findAll();
            List<RuleLogicFieldInfo> logicFields = ruleLogicFieldInfoService.findAll();

            // 将所有信息按ruleId分类
            RuleIdTable ruleIdTable = new RuleIdTable(ruleFields, outFields, logicFields);

            // 分别放入对应的业务BizRuleWrap
            Map<String, BizRuleWrapInner> newBizMap = new HashMap<String, BizRuleWrapInner>();
            String defaultVersion = WagonConstants.DATE_TIME_FORMAT_PURE.format(new Date());
            for (RuleInfo ruleInfo : ruleInfos) {
                BizRuleWrapInner wrapInner = newBizMap.get(ruleInfo.getBizName());
                if (wrapInner == null) {
                    RuleBizInfo ruleBizInfo = allBizMap.get(ruleInfo.getBizName());
                    String bizVersion = StringUtils.isBlank(ruleBizInfo.getVersion()) ? defaultVersion : ruleBizInfo.getVersion();
                    wrapInner = new BizRuleWrapInner(new BizRuleWrap(ruleInfo.getBizName()), bizVersion);
                    newBizMap.put(ruleInfo.getBizName(), wrapInner);
                }

                List<TopicRule> topicRules = toTopicRules(ruleInfo, ruleIdTable);
                for (TopicRule topicRule : topicRules) {
                    wrapInner.wrap.add(topicRule);
                }
            }

            // 有biz信息,但该biz下没有规则的,也加入
            for (Map.Entry<String, RuleBizInfo> entry : allBizMap.entrySet()) {
                String bizName = entry.getKey();
                RuleBizInfo ruleBizInfo = entry.getValue();
                String bizVersion = StringUtils.isBlank(ruleBizInfo.getVersion()) ? defaultVersion : ruleBizInfo.getVersion();
                if (!newBizMap.containsKey(bizName)) {
                    BizRuleWrapInner wrapInner = new BizRuleWrapInner(new BizRuleWrap(bizName), bizVersion);
                    newBizMap.put(bizName, wrapInner);
                }
            }

            /**
             * <pre>
             * 如果业务对应版本未变化(可能规则数据有变化，因为没有手动对该业务做版本更新)，则重新设置为老的规则数据 
             * 注意，之所以未在数据层实现该过滤功能，是为了使数据层对版本变化判断的逻辑更简单些
             * </pre>
             */
            for (Entry<String, BizRuleWrapInner> entry : newBizMap.entrySet()) {
                BizRuleWrapInner newBizWrap = entry.getValue();
                BizRuleWrapInner oldBizWrap = bizMap.get(entry.getKey());
                if (oldBizWrap != null && newBizWrap.version.equals(oldBizWrap.version)) { // 版本未变化
                    newBizMap.put(entry.getKey(), oldBizWrap); // 重新设置为老的规则数据
                }
            }

            // 替换bizMap
            try {
                this.lock.writeLock().lockInterruptibly();
                this.bizMap = newBizMap;
            } finally {
                this.lock.writeLock().unlock();
            }
            LOG_HEARTBEAT.info("update rule, update completed.");
        } else {
            LOG_HEARTBEAT.info("update rule, no need update.");
        }
    }

    // ========================= help method ========== ======================

    /**
     * <pre>
     * 是否需要更新，符合以下条件之一则更新，且将数据库的全量更新到bizMap
     * 1，RuleBizInfo有新增或删除
     * 2，RuleBizInfo版本有更新
     * </pre>
     *
     * @return
     */
    private boolean isNeedUpdate(Map<String, RuleBizInfo> allBizMap) {
        // 有新增
        for (String bizName : allBizMap.keySet()) {
            if (!bizMap.containsKey(bizName)) {
                return true;
            }
        }

        // 有删除
        for (String bizName : bizMap.keySet()) {
            if (!allBizMap.containsKey(bizName)) {
                return true;
            }
        }

        // 版本有更新
        for (Entry<String, BizRuleWrapInner> entry : bizMap.entrySet()) {
            BizRuleWrapInner curBiz = entry.getValue();
            RuleBizInfo updatedBiz = allBizMap.get(entry.getKey());
            if (updatedBiz != null && !curBiz.version.equals(updatedBiz.getVersion())) {
                return true;
            }
        }

        return false;
    }

    private List<TopicRule> toTopicRules(RuleInfo ruleInfo, RuleIdTable ruleIdTable) {
        List<TopicRule> topicRules = new ArrayList<TopicRule>();
        String[] eventTypes = ruleInfo.getEventType().split(",");

        for (String eventType : eventTypes) {
            TopicRule topicRule = new TopicRule(ruleInfo.getTableName(), ruleInfo.getTopicName(), string2EventType(eventType));
            topicRules.add(topicRule);
            topicRule.setId(String.valueOf(ruleInfo.getId()));

            topicRule.setActive(ruleInfo.getEnableState() == WagonConstants.RULE_ENABLE_STATE_ENABLED ? true : false);
            topicRule.setPushBeforeData(ruleInfo.getPushDataType() == WagonConstants.RULE_PUSH_DATA_TYPE_ALL ? true : false);
            topicRule.setFieldRuleRela(ruleInfo.getFieldConditionType() == WagonConstants.RULE_FIELD_CONDITION_AND ? "and" : "or");
            topicRule.setOutFieldIgnore(ruleInfo.getOutType() == WagonConstants.RULE_OUT_TYPE_IGNORE ? true : false);

            topicRule.setMaxRetryCount(ruleInfo.getMaxRetryCount());
            topicRule.setSeqConsumeField(ruleInfo.getSeqConsumeField());

            List<FieldRule> fieldRuleList = topicRule.getFieldRuleList();
            List<String> outFieldList = topicRule.getOutFieldList();
            List<LogicRule> logicRuleList = topicRule.getLogicRuleList();

            for (RuleFieldInfo fieldInfo : ruleIdTable.fieldMap.get(ruleInfo.getId())) {
                FieldRule fieldRule = new FieldRule(fieldInfo.getFieldName(), fieldInfo.getFieldOperator(), fieldInfo.getFieldValue());
                fieldRule.setBeforeOrAfter(fieldInfo.getPositionType() == WagonConstants.RULE_FIELD_POSITION_AFTER ? "after" : "before");
                fieldRule.setMustUpdated(fieldInfo.isMustUpdated());
                fieldRuleList.add(fieldRule);
            }

            for (RuleOutFieldInfo outField : ruleIdTable.outFieldMap.get(ruleInfo.getId())) {
                outFieldList.add(outField.getFieldName());
            }

            for (RuleLogicFieldInfo logicField : ruleIdTable.logicFieldMap.get(ruleInfo.getId())) {
                LogicRule logicRule = new LogicRule(logicField.getFieldName(), logicField.getFieldOperator(), logicField.getFieldValue());
                logicRule.setDataType(logicField.getDataType());
                logicRuleList.add(logicRule);
            }
        }

        return topicRules;
    }

    private EventType string2EventType(String eventType) {
        if (WagonConstants.EVENT_TYPE_INSERT.equalsIgnoreCase(eventType)) {
            return EventType.INSERT;
        } else if (WagonConstants.EVENT_TYPE_UPDATE.equalsIgnoreCase(eventType)) {
            return EventType.UPDATE;
        } else if (WagonConstants.EVENT_TYPE_DELETE.equalsIgnoreCase(eventType)) {
            return EventType.DELETE;
        }
        throw new WagonException("不合法的事件类型：" + eventType);
    }

    private class BizRuleWrapInner {
        private BizRuleWrap wrap;
        private String version;

        public BizRuleWrapInner(BizRuleWrap wrap, String version) {
            this.wrap = wrap;
            this.version = version;
        }

    }

    private class RuleIdTable {
        private Map<Long/* ruleId */, List<RuleFieldInfo>> fieldMap = MigrateMap.makeComputingMap(new Function<Long, List<RuleFieldInfo>>() {
            public List<RuleFieldInfo> apply(Long ruleId) {
                return new ArrayList<RuleFieldInfo>();
            }
        });
        private Map<Long/* ruleId */, List<RuleOutFieldInfo>> outFieldMap = MigrateMap.makeComputingMap(new Function<Long, List<RuleOutFieldInfo>>() {
            public List<RuleOutFieldInfo> apply(Long ruleId) {
                return new ArrayList<RuleOutFieldInfo>();
            }
        });
        private Map<Long/* ruleId */, List<RuleLogicFieldInfo>> logicFieldMap = MigrateMap.makeComputingMap(new Function<Long, List<RuleLogicFieldInfo>>() {
            public List<RuleLogicFieldInfo> apply(Long ruleId) {
                return new ArrayList<RuleLogicFieldInfo>();
            }
        });

        private RuleIdTable(List<RuleFieldInfo> ruleFields, List<RuleOutFieldInfo> outFields, List<RuleLogicFieldInfo> logicFields) {
            if (CollectionUtils.isNotEmpty(ruleFields)) {
                for (RuleFieldInfo field : ruleFields) {
                    fieldMap.get(field.getRuleId()).add(field);
                }
            }

            if (CollectionUtils.isNotEmpty(outFields)) {
                for (RuleOutFieldInfo outField : outFields) {
                    outFieldMap.get(outField.getRuleId()).add(outField);
                }
            }

            if (CollectionUtils.isNotEmpty(logicFields)) {
                for (RuleLogicFieldInfo logicField : logicFields) {
                    logicFieldMap.get(logicField.getRuleId()).add(logicField);
                }
            }
        }

    }

}
