package com.youzan.wagon.filter.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.youzan.wagon.common.WagonConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.common.utils.PropertiesManager;

public class FilterConfigUtil {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger("HEARTBEAT");

    private static Set<String> debugTableSet = new HashSet<String>();
    private static AtomicLong lastUpdateTime = new AtomicLong();

    public static boolean needDebug(String table) {
        if ((System.currentTimeMillis() - lastUpdateTime.get()) > 60000) {
            lastUpdateTime.set(System.currentTimeMillis());
            load();
        }

        return !debugTableSet.isEmpty() && debugTableSet.contains(table);
    }

    private static void load() {
        try {
            Set<String> tmp = new HashSet<String>();
            String tables = PropertiesManager.get("debug.tabes");
            if (StringUtils.isNotBlank(tables)) {
                String[] tableArray = tables.trim().split(",");
                if (tableArray != null && tableArray.length > 0) {
                    for (String table : tableArray) {
                        tmp.add(table.trim());
                    }
                }
            }

            debugTableSet = tmp;
            LOG_HEARTBEAT.info("reload {}, tables: {}", PropertiesManager.getConfigFileName(), tables);
        } catch (Exception e) {
            LOG_ERROR.error("reload {} failed: {}", PropertiesManager.getConfigFileName(), ExceptionUtils.getFullStackTrace(e));
        }
    }

}
