package com.youzan.wagon.filter.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.youzan.wagon.common.rule.TableRule;

public class RuleContainer {
    private String rules;
    private Map<String, TableRule> ruleTable = new HashMap<String, TableRule>();

    private static RuleContainer singleton;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private RuleContainer() {
    }

    public static RuleContainer instance() {
        if (singleton == null) {
            synchronized (RuleContainer.class) {
                if (singleton == null) {
                    singleton = new RuleContainer();
                }
            }
        }

        return singleton;
    }

    public String getRules() {
        try {
            this.lock.readLock().lock();
            return rules;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public TableRule getTableRule(String tableName) {
        try {
            this.lock.readLock().lock();
            return ruleTable.get(tableName);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Set<String> getTableNames() {
        try {
            this.lock.readLock().lock();
            return ruleTable.keySet();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void update(String json) throws JsonParseException, JsonMappingException, IOException {
        // 需要改造
        if (StringUtils.isBlank(json)) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TableRule[] tableRules = objectMapper.readValue(json, TableRule[].class);
        Map<String, TableRule> temp = new HashMap<String, TableRule>();
        for (TableRule tableRule : tableRules) {
            temp.put(tableRule.getTableName(), tableRule);
        }

        try {
            this.lock.writeLock().lock();
            ruleTable = temp;
            rules = json;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
