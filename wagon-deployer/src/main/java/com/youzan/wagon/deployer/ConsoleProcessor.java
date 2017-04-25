package com.youzan.wagon.deployer;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.otter.canal.common.WagonConstants;
import com.alibaba.otter.canal.common.protocol.CanalServiceData;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.youzan.wagon.filter.container.RuleContainer;
import com.youzan.wagon.filter.exception.RuleFilterException;

public class ConsoleProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleProcessor.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger(WagonConstants.LOG_NAME_HEARTBEAT);

    private CanalServiceData canalServiceData;
    private final ConsumeController consumeController;
    private final String syncRuleURL;
    private RuleContainer ruleContainer = RuleContainer.instance();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "WagonSchedulerThread-" + count.getAndIncrement());
        }
    });

    public ConsoleProcessor(Properties properties, ConsumeController consumeController) throws IOException {
        this.consumeController = consumeController;
        this.canalServiceData = consumeController.getCanalServiceData();
        String consoleAddr = PropertiesManager.getProperty(WagonConstants.PRO_CONSOLE_ADDRESS);

        String bizName = null;
        if (canalServiceData != null) {
            bizName = canalServiceData.getRuleBizName();
        }
        if (StringUtils.isNotBlank(PropertiesManager.getProperty(WagonConstants.PRO_BIZ_NAME))) {
            bizName = PropertiesManager.getProperty(WagonConstants.PRO_BIZ_NAME);
        }
        if (StringUtils.isBlank(consoleAddr) || StringUtils.isBlank(bizName)) {
            throw new RuleFilterException(String.format("Illegal param, consoleAddr=%s, bizName=%s", consoleAddr, bizName));
        }
        LOG.info("consoleAddr={}; bizName={}", consoleAddr, bizName);

        syncRuleURL = getHttpURL(consoleAddr, "syncRules", String.format("?bizName=%s", bizName));
        LOG.info("syncRuleURL=[{}]", syncRuleURL);
    }

    public void process() throws IOException {
        initSubscribe();// 初始化rule
        startSchedule(); // 启动定时任务
    }

    private void initSubscribe() throws IOException {
        String json = HttpClient.sendGet(syncRuleURL);
        LOG_HEARTBEAT.info("Get init sync rule:\n{}", json);
        if (StringUtils.isBlank(json)) {
            return;
        }

        // 更新规则管理器
        ruleContainer.update(json);

        // 对各个实例的消费服务执行订阅
        String subscribe = getSubscribe(ruleContainer.getTableNames());
        if (consumeController.getConsumeServiceTable() != null && StringUtils.isNotBlank(subscribe)) {
            LOG_HEARTBEAT.info("Init Subscribe, tables:\n{}", subscribe);
            for (CanalInstanceConsumeService consumeService : consumeController.getConsumeServiceTable().values()) {
                consumeService.reSubscribe(subscribe);
            }
        }
    }

    private void startSchedule() {
        // 同步规则
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                syncRule();
            }
        }, 0, PropertiesManager.getInteger(WagonConstants.PRO_RULE_SYNC_INTERVAL, WagonConstants.DERAULT_SCHEDULE_INTERVAL), TimeUnit.SECONDS);
    }

    private void syncRule() {
        try {
            // 向console发送请求，获取rule数据
            String json = HttpClient.sendGet(syncRuleURL);

            // rule数据为空，则直接返回，保护因为失误把规则清空
            if (StringUtils.isBlank(json)) {
                LOG_HEARTBEAT.info("Get empty sync rule:{}\n", json);
                return;
            } else if (PropertiesManager.getBoolean(WagonConstants.PRO_RULE_PRINT_EVERYTIME, false)) {
                LOG_HEARTBEAT.info("Get sync rule:{}\n", json);
            }

            // 判断rule是否有变更(策略需要再考虑)
            boolean changed = false;
            if (StringUtils.isBlank(ruleContainer.getRules())) {
                changed = true;
            } else {
                String oldRulesMD5 = DigestUtils.md5Hex(ruleContainer.getRules());
                String newRulesMD5 = DigestUtils.md5Hex(json);
                changed = !oldRulesMD5.equals(newRulesMD5);
            }

            // 更新或者只打印
            if (changed) {
                LOG_HEARTBEAT.info("Get changed sync rule:{}\n", json);

                // 更新规则管理器
                Set<String> oldNames = ruleContainer.getTableNames();
                ruleContainer.update(json);
                Set<String> newNames = ruleContainer.getTableNames();

                // 对各个实例的消费服务执行重新订阅(如果表名列表有变化)
                if (!(oldNames.containsAll(newNames) && newNames.containsAll(oldNames))) {
                    String subscribe = getSubscribe(newNames);
                    if (consumeController.getConsumeServiceTable() != null && StringUtils.isNotBlank(subscribe)) {
                        LOG_HEARTBEAT.info("Do reSubscribe, tables:\n{}", subscribe);
                        for (CanalInstanceConsumeService consumeService : consumeController.getConsumeServiceTable().values()) {
                            consumeService.reSubscribe(subscribe);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG_ERROR.error("syncRule failed, url={}, cause by: {}", syncRuleURL, ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * 将表名列表转化为订阅正则表达式，如：表名为goods_v2,order_v2，则转化后的表达式为".*\\.,goods_v2.*\\.
     * order_v2"，即，订阅表goods_v2和order_v2的变更数据，而不关心这两张表属于哪个库
     *
     * @param tableNames
     * @return
     */
    private String getSubscribe(Set<String> tableNames) {
        if (tableNames == null || tableNames.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String tableName : tableNames) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(".*\\.").append(tableName);
            i++;
        }
        return sb.toString();
    }

    private String getHttpURL(String addr, String simpleUrl, String param) {
        assert StringUtils.isNotBlank(addr);

        StringBuilder url = new StringBuilder(addr);

        if (StringUtils.isNotBlank(simpleUrl)) {
            if (!addr.endsWith("/")) {
                url.append("/");
            }
            url.append(simpleUrl);
        }

        if (StringUtils.isNotBlank(param)) {
            url.append(param);
        }

        return url.toString();
    }


    // ======================= get and set ==================================
    public CanalServiceData getCanalServiceData() {
        return canalServiceData;
    }

    public void setCanalServiceData(CanalServiceData canalServiceData) {
        this.canalServiceData = canalServiceData;
    }

}
