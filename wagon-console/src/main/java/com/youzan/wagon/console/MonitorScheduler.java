package com.youzan.wagon.console;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.common.utils.NamedThreadFactory;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.youzan.wagon.common.SpringHelper;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.youzan.wagon.persistent.model.CanalInstanceInfo;
import com.youzan.wagon.persistent.model.CanalRunningInfoWrapper;
import com.youzan.wagon.persistent.mvc.service.CanalRunningStatusMvcService;
import com.youzan.wagon.persistent.service.CanalInstanceInfoService;

/**
 * 监控canal是否健康运行，并报警
 * 
 * @author wangguofeng since 2016年3月17日 下午2:00:42
 */
public class MonitorScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorScheduler.class);
    private static final Logger LOG_HEARTBEAT = LoggerFactory.getLogger("HEARTBEAT");

    // property key
    private static final String PROPERTY_KEY_SCHEDULER_INTERVAL = "monitor.scheduler.interval";
    private static final String PROPERTY_KEY_HEARTBEAT_TIMEOUT = "monitor.canal.heartbeat.timeout";
    private static final String PROPERTY_KEY_MAX_ALARM_TIMES = "monitor.max.alarm.times";

    private static final String PROPERTY_KEY_THRESHOLD_REMAIN_BUFFER_SIZE = "monitor.threshold.remain.buffer.size";
    private static final String PROPERTY_KEY_THRESHOLD_REMAIN_BUFFER_CAPACITY = "monitor.threshold.remain.buffer.capacity";
    private static final String PROPERTY_KEY_THRESHOLD_THRESHOLD_CONSUME_DELAY = "monitor.threshold.consume.delay";

    private int intervalInSecond = 60; // 监控轮训时间间隔(s)
    private int canalHeartbeatTimeout = 180; // canal心跳超时时间(超过该时间没有心跳消息则报警)
    private int maxAlarmTimes = 3; // 最大连续报警次数，连续报警超过该次数则不再报警

    private int thresholdRemainBufferSize = 1000; // 报警阀值，canal缓存可以条数少于该值则报警
    private int thresholdRemainBufferCapacity = 1024; // 报警阀值，canal缓存容量小于该值则报警
    private int thresholdConsumeDelay = 10 * 24 * 3600 * 1000; // 报警阀值，canal客户端消费延迟超过该值则报警

    private Map<CanalInstanceInfo, AtomicInteger> canalHeartbeatAlarmTimes;
    private Map<CanalInstanceInfo, AtomicInteger> canalRunningStatusAlarmTimes;

    private CanalInstanceInfoService canalInstanceInfoService;
    private CanalRunningStatusMvcService canalRunningInfoService;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("MonitorScheduler"));;

    public MonitorScheduler() {
        canalInstanceInfoService = SpringHelper.getBean("canalInstanceInfoService");
        canalRunningInfoService = SpringHelper.getBean("canalRunningInfoService");

        canalHeartbeatAlarmTimes = MigrateMap.makeComputingMap(new Function<CanalInstanceInfo, AtomicInteger>() {
            public AtomicInteger apply(CanalInstanceInfo input) {
                return new AtomicInteger(0);
            }
        });
        canalRunningStatusAlarmTimes = MigrateMap.makeComputingMap(new Function<CanalInstanceInfo, AtomicInteger>() {
            public AtomicInteger apply(CanalInstanceInfo input) {
                return new AtomicInteger(0);
            }
        });

        intervalInSecond = PropertiesManager.getInteger(PROPERTY_KEY_SCHEDULER_INTERVAL, intervalInSecond);
        canalHeartbeatTimeout = PropertiesManager.getInteger(PROPERTY_KEY_HEARTBEAT_TIMEOUT, canalHeartbeatTimeout);
        maxAlarmTimes = PropertiesManager.getInteger(PROPERTY_KEY_MAX_ALARM_TIMES, maxAlarmTimes);
        thresholdRemainBufferSize = PropertiesManager.getInteger(PROPERTY_KEY_THRESHOLD_REMAIN_BUFFER_SIZE, thresholdRemainBufferSize);
        thresholdRemainBufferCapacity = PropertiesManager.getInteger(PROPERTY_KEY_THRESHOLD_REMAIN_BUFFER_CAPACITY, thresholdRemainBufferCapacity);
        thresholdConsumeDelay = PropertiesManager.getInteger(PROPERTY_KEY_THRESHOLD_THRESHOLD_CONSUME_DELAY, thresholdConsumeDelay);
        LOG.info(String.format("properties:[intervalInSecond=%d, canalHeartbeatTimeout=%d, maxAlarmTimes=%d]", intervalInSecond, canalHeartbeatTimeout, maxAlarmTimes));
    }

    public void start() {
        executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // 监控canal实例是否正常运行
                try {
                    monitorCanalInstanceStatus();
                    LOG_HEARTBEAT.info("monitorCanalInstanceStatus completed.");
                } catch (Exception e) {
                    LOG.error("Something goes wrong at monitorCanalInstanceStatus:\n{}", ExceptionUtils.getFullStackTrace(e));
                }

                // 监控canal的运行状态是否健康，如，客户消费延时等
                try {
                    monitorCanalRunningStatus();
                    LOG_HEARTBEAT.info("monitorCanalRunningStatus completed.");
                } catch (Exception e) {
                    LOG.error("Something goes wrong at monitorCanalRunningStatus:\n{}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 0, intervalInSecond, TimeUnit.SECONDS);
        LOG.info("MonitorScheduler have ben started.");
    }

    /**
     * 监控canal实例是否正常运行
     */
    private void monitorCanalInstanceStatus() {
        // 查找所有实例
        List<CanalInstanceInfo> allInstances = null;
        try {
            allInstances = canalInstanceInfoService.findAll();
        } catch (Exception e) {
            String msg = String.format("monitorCanalInstanceStatus failed, cause by finding all canal instance failed: \n%s", ExceptionUtils.getFullStackTrace(e));
            LOG.error(msg);
            AtomicInteger alarmTimes = canalHeartbeatAlarmTimes.get(new CanalInstanceInfo("NULL", 0, "NULL"));
            if (alarmTimes.getAndIncrement() < maxAlarmTimes) { // 报警次数未达上限
                doAlarm(msg); // 报警
            }
            return;
        }

        if (allInstances != null) {
            for (CanalInstanceInfo instance : allInstances) {
                String msg = null;
                try {
                    if (instance.getModifyTime() != null && //
                            (System.currentTimeMillis() - instance.getModifyTime().getTime()) >= canalHeartbeatTimeout * 1000) {
                        // 心跳超时
                        msg = String.format("canal instance heartbeat timeout, instance info:[%s]", instance.toString());
                    } else {
                        // 正常心跳，重置为0
                        canalHeartbeatAlarmTimes.get(instance).set(0);
                    }
                } catch (Exception e) {
                    msg = String.format("monitorCanalInstanceStatus failed, instance info:[%s], cause by: \n%s", instance.toString(), ExceptionUtils.getFullStackTrace(e));
                }

                // 需要报警
                if (msg != null) {
                    LOG.error(msg);
                    if (canalHeartbeatAlarmTimes.get(instance).getAndIncrement() < maxAlarmTimes) {
                        doAlarm(msg); // 报警
                    }
                }
            }
        }
    }

    /**
     * 监控canal的运行状态是否健康，如，客户消费延时等
     */
    private void monitorCanalRunningStatus() {
        // 获取所有canal实例运行信息
        CanalRunningInfoWrapper wrapper = null;
        try {
            wrapper = canalRunningInfoService.canalRunningInfoList(null);
        } catch (Exception e) {
            String msg = String.format("monitorCanalRunningStatus failed, cause by queryCanalRunningInfo failed: \n%s", ExceptionUtils.getFullStackTrace(e));
            LOG.error(msg);
            if (canalRunningStatusAlarmTimes.get(new CanalInstanceInfo("null", 0, "null")).getAndIncrement() < maxAlarmTimes) { // 报警次数未达上限
                doAlarm(msg); // 报警
            }
            return;
        }

        if (wrapper != null) {
        }
    }

    public void stop() {
        LOG.info("stoping MonitorScheduler ...");
        executor.shutdown();
        LOG.info("MonitorScheduler have ben stop.");
    }

    // ================== setter / getter ===================

    private void doAlarm(String content) {
        LOG.error(content);
    }

}
