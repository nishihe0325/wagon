package com.youzan.wagon.console.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.youzan.wagon.common.util.StringUtils;

/**
 * @author wangguofeng since 2016年11月3日 下午7:29:11
 */
@Component("bizTableManager")
public class BizTableManager {
    private Map<String /* bizName */, Set<String/* tableName */>> bizTableMap = new HashMap<String, Set<String>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private BizTableManager() {
        Set<String> tables = new HashSet<String>();
        tables.add("fans_order");
        tables.add("buyer_order");
        tables.add("fans");
        bizTableMap.put("fans", tables);

        tables = new HashSet<String>();
        tables.add("trade_order");
        tables.add("trade");
        bizTableMap.put("trade", tables);

        tables = new HashSet<String>();
        tables.add("ump_groupon_group");
        tables.add("bind_youzan_to_fans");
        bizTableMap.put("date", tables);

        tables = new HashSet<String>();
        tables.add("supplier_goods");
        tables.add("order_fenxiao");
        bizTableMap.put("fenxiao", tables);
    }

    public Set<String> getTables(String bizName) {
        try {
            this.lock.readLock().lock();
            return bizTableMap.get(bizName);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Set<String> bizNames(String tableName) {
        Set<String> bizNames = new HashSet<String>();
        try {
            this.lock.readLock().lock();
            for (Map.Entry<String, Set<String>> entry : bizTableMap.entrySet()) {
                if (entry.getValue().contains(tableName)) {
                    bizNames.add(entry.getKey());
                }
            }
            return bizNames;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void update(String bizName, Set<String> tables) {
        if (StringUtils.isBlank(bizName) || CollectionUtils.isEmpty(tables)) {
            return;
        }

        try {
            this.lock.writeLock().lock();
            bizTableMap.put(bizName, tables);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
