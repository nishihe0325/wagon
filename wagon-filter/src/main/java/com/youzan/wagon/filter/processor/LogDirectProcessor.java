package com.youzan.wagon.filter.processor;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.util.ProcessorUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.youzan.wagon.common.processor.MQService;
import com.youzan.wagon.common.processor.SyncProcessor;
import com.youzan.wagon.common.rule.TableRule;
import com.youzan.wagon.filter.container.RuleContainer;
import com.youzan.wagon.filter.utils.FilterConfigUtil;

public class LogDirectProcessor implements SyncProcessor {
    private static final Logger LOG_STORE = LoggerFactory.getLogger(WagonConstants.LOG_NAME_STORE);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);
    private static final Logger LOG_DEGUG = LoggerFactory.getLogger(WagonConstants.LOG_NAME_DEBUG);

    private RuleContainer ruleContainer = RuleContainer.instance();

    @Override
    public boolean process(List<Entry> entries) {
        if (CollectionUtils.isNotEmpty(entries)) {
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

    private void process(Entry entry) throws Exception {
        if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
            return;
        }

        CanalEntry.Header header = entry.getHeader();
        String table = header.getTableName();
        if (StringUtils.isBlank(table)) {
            return;
        }

        /**
         * 判断是否符合条件,不符合条件的情况,需要同时满足以下条件(否则满足条件)
         * 1,容器中至少配置有一个规则(如果没有任何规则,则说明所有表都满足)
         * 2,没有配置该表的规则,或该表的规则已暂停
         */
        boolean needDebug = FilterConfigUtil.needDebug(table);
        if (CollectionUtils.isNotEmpty(ruleContainer.getTableNames())) {
            TableRule tableRule = ruleContainer.getTableRule(table);
            if (tableRule == null || !tableRule.isActive()) { // 没有配置该表的规则,或该表的规则已暂停
                if (needDebug) {
                    LOG_DEGUG.debug("has no table rule or table rule stopped, table={}", table);
                }
                return;
            }
        }

        // 过滤出符合事件类型并未暂停的规则
        RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
        EventType eventType = rowChange.getEventType();
        for (RowData row : rowChange.getRowDatasList()) {
            Map<String, Object> beforeMap = ProcessorUtils.feedMap(row.getBeforeColumnsList());
            Map<String, Object> afterMap = ProcessorUtils.feedMap(row.getAfterColumnsList());

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("before", beforeMap);
            map.put("after", afterMap);

            // 添加其他属性，并转化为json
            map.put("add_mq_time", System.currentTimeMillis());
            map.put("eventType", eventType.name());
            map.put("binlogFileName", header.getLogfileName());
            map.put("binlogOffset", header.getLogfileOffset());
            map.put("binlogTime", header.getExecuteTime());
            map.put("binlogTableName", header.getTableName());
            map.put("binlogSchemaName", header.getSchemaName());

            handleRow(map);
        }
    }

    private void handleRow(Map<String, Object> map) {
        while (true) { // 无限重试,直到处理成功
            String message = "";
            try {
                message = JSON.toJSONString(map, SerializerFeature.BrowserCompatible);
                LOG_STORE.info(message);
                return; // 打印成功则返回
            } catch (Exception e) {
                LOG_ERROR.error("handleRow failed, message={}, cause by:{}\n", message, ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

}
