package com.youzan.wagon.filter.processor;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.processor.MQService;
import com.youzan.wagon.common.processor.SyncProcessor;
import com.youzan.wagon.common.rule.TableRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.filter.bean.RowDataBean;
import com.youzan.wagon.filter.container.RuleContainer;
import com.youzan.wagon.filter.converter.MessageConverter;
import com.youzan.wagon.filter.converter.MessageConverterFactory;
import com.youzan.wagon.filter.matcher.TopicMatcher;
import com.youzan.wagon.filter.matcher.TopicMatcherFactory;
import com.youzan.wagon.filter.utils.FilterConfigUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NsqProcessor implements SyncProcessor {
    private static final Logger LOG_DEGUG = LoggerFactory.getLogger(WagonConstants.LOG_NAME_DEBUG);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private MQService mqService;
    private RuleContainer ruleContainer = RuleContainer.instance();
    private TopicMatcher topicMatcher = TopicMatcherFactory.getTopicMatcher();
    private MessageConverter messageConverter = MessageConverterFactory.getMessageConverter();

    public NsqProcessor(MQService mqService) throws Throwable {
        this.mqService = mqService;
    }

    @Override
    public boolean process(List<Entry> entries) {
        if (entries != null && !entries.isEmpty()) {
            for (Entry entry : entries) {
                try {
                    process(entry);
                } catch (Throwable e) {
                    LOG_ERROR.error("EntryProcessor.process goes wrong at:{}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        }

        return true;
    }

    /**
     * 处理某一提交事务的变更，先从系统配置的所有规则中筛选出表名和事件类型匹配的规则列表，然后判断每条变更数据是否满足这些规则中的具体某条规则，如果满足
     * ，则将该记录推送到对应的主题
     *
     * @param entry
     * @throws Exception
     */
    private void process(Entry entry) throws Exception {
        if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
            return;
        }

        CanalEntry.Header header = entry.getHeader();
        String table = header.getTableName();
        if (StringUtils.isBlank(table)) {
            return;
        }
        boolean needDebug = FilterConfigUtil.needDebug(table);

        // 事件对应表是否配置了规则
        TableRule tableRule = ruleContainer.getTableRule(table);
        if (tableRule == null) {
            if (needDebug) {
                LOG_DEGUG.debug("has no table rule, table={}", table);
            }
            return;
        }

        // 是否表的所有规则都已暂停
        if (!tableRule.isActive()) {
            if (needDebug) {
                LOG_DEGUG.debug("table rule stopped, table={}", table);
            }
            return;
        }

        // 该表未配置任何规则
        List<TopicRule> topicRuleList = tableRule.getTopicRuleList();
        if (topicRuleList == null || topicRuleList.size() == 0) {
            if (needDebug) {
                LOG_DEGUG.debug("table has no rules, table={}", table);
            }
            return;
        }

        // 过滤出符合事件类型并未暂停的规则
        RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
        EventType eventType = rowChange.getEventType();
        Set<TopicRule> rules = new HashSet<TopicRule>();
        for (TopicRule topicRule : topicRuleList) {
            if (!(eventType == topicRule.getEventType())) {
                if (needDebug) {
                    LOG_DEGUG.debug("type not matched, type={}, topic={}", eventType, topicRule.getTopicName());
                }
                continue;
            }
            if (!topicRule.isActive()) {
                if (needDebug) {
                    LOG_DEGUG.debug("rule stopped, topic={}", topicRule.getTopicName());
                }
                continue;
            }

            rules.add(topicRule);
        }

        // 过滤后，没有符合的规则
        if (rules.size() == 0) {
            if (needDebug) {
                LOG_DEGUG.debug("has no effective rule, table={}, eventType={}", table, eventType);
            }
            return;
        }

        process(rowChange, rules, header);
    }

    /**
     * 处理某一提交事务的所有变更行，该提交可能涉及多行，需要对每行进行判断
     *
     * @param rowChange
     * @param topicRules
     * @throws Exception
     */
    private void process(RowChange rowChange, Set<TopicRule> topicRules, CanalEntry.Header header) throws Exception {
        for (RowData row : rowChange.getRowDatasList()) {
            process(row, topicRules, rowChange.getEventType(), header);
        }
    }

    /**
     * 处理变更的具体行，该行可能符合不同规则，遍历这些规则列表，只要符合，变会推送到该规则对应的topic，符合多个规则，则推送多个topic
     *
     * @param row        :具体的某一行
     * @param topicRules :该行可能会符合的规则列表(具体是否符合，还需要看列值是否符合)
     * @throws IOException
     */
    private void process(RowData row, Set<TopicRule> topicRules, EventType eventType, CanalEntry.Header header) throws Exception {
        RowDataBean bean = new RowDataBean(row);
        for (TopicRule topicRule : topicRules) {
            if (topicMatcher.isMatched(topicRule, bean)) {
                String message = messageConverter.conver(bean, topicRule, eventType, header);
                Long shardingId = getShardingId(topicRule, bean, header);
                if (shardingId != null) {
                    shardingId = Math.abs(shardingId);
                }
                mqService.put(message, topicRule, shardingId);
            }
        }
    }

    private Long getShardingId(TopicRule topicRule, RowDataBean bean, CanalEntry.Header header) throws Exception {
        String seqConsumeField = topicRule.getSeqConsumeField();
        if (StringUtils.isBlank(seqConsumeField)) {
            return null; // 没有配置顺序消费字段(不需要顺序消费)
        }

        // 获取顺序字段对应的值
        CanalEntry.Column column = null;
        EventType eventType = topicRule.getEventType();
        if (eventType == EventType.INSERT || eventType == EventType.UPDATE) {
            column = bean.getAfterColumnsMap().get(seqConsumeField);
        } else if (eventType == EventType.DELETE) {
            column = bean.getBeforeColumnsMap().get(seqConsumeField);
        }

        if (column == null) {
            String message = messageConverter.conver(bean, topicRule, eventType, header);
            LOG_ERROR.error("the column of sequential field is null, topic={}, sequentialField={} message={}", topicRule.getTopicName(), seqConsumeField, message);
        } else {
            String columnValue = column.getValue();
            if (StringUtils.isBlank(columnValue)) {
                String message = messageConverter.conver(bean, topicRule, eventType, header);
                LOG_ERROR.error("the column value of sequential field is blank, topic={}, sequentialField={}, message={}", topicRule.getTopicName(), seqConsumeField, message);
            } else {
                return Long.valueOf(columnValue.hashCode());
            }
        }

        return null;
    }

}
