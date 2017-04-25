package com.youzan.wagon.console;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.youzan.wagon.common.WagonConstants;
import com.alibaba.otter.canal.common.utils.PropertiesManager;

@Component("consoleScheduler")
public class ConsoleScheduler {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger("ERROR");

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ConsoleSchedulerThread-" + count.getAndIncrement());
        }
    });

    // ======================================================================================
    @Resource
    private RuleManager ruleManager;
    @Resource
    private CmdbService cmdbService;
    @Resource
    private OrganizationManager organizationManager;

    // ======================================================================================
    @PostConstruct
    public void startSchedule() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                updateRuleManager();
            }
        }, 0, PropertiesManager.getInteger(WagonConstants.PRO_UPDATE_RULE_MANAGER_INTERVAL, 1), TimeUnit.MINUTES);

        // 更新cmdb信息到本地缓存
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                updateFromCMDB();
            }
        }, 0, PropertiesManager.getInteger(WagonConstants.PRO_INTERVAL_UPDATE_FROM_CMDB, WagonConstants.DERAULT_INTERVAL_UPDATE_FROM_CMDB), TimeUnit.MINUTES);
//
//        // 更新oa信息到本地缓存
//        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            public void run() {
//                updateFromOA();
//            }
//        }, 0, PropertiesManager.getInteger(WagonConstants.PRO_INTERVAL_UPDATE_FROM_OA, WagonConstants.DERAULT_INTERVAL_UPDATE_FROM_OA), TimeUnit.MINUTES);
    }

    // ============================= task ====================================
    private void updateRuleManager() {
        try {
            ruleManager.update();
        } catch (Throwable e) {
            LOG_ERROR.error("updateRuleManager failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void updateFromCMDB() {
        try {
            cmdbService.updateFromCMDB();
        } catch (Throwable e) {
            LOG_ERROR.error("updateFromCMDB failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void updateFromOA() {
        try {
            organizationManager.updateFromOA();
        } catch (Throwable e) {
            LOG_ERROR.error("updateFromOA failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        }
    }

}
