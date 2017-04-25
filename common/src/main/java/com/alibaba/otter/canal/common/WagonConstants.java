package com.alibaba.otter.canal.common;

public final class WagonConstants {

    private WagonConstants() {
    }

    // ======================= 请求URL =============================
    public static final String URL_GET_CANAL_SERVICE_DATAS = "/canal/getCanalServiceDatas.json";
    public static final String URL_GET_CANAL_INSTANCE_DATAS = "/canal/getCanalInstanceDatas.json";
    public static final String URL_UPDATE_INSTANCE_DATAS = "/canal/updateInstanceDatas.json";
    public static final String URL_REGISTER_INSTANCE = "/canal/registerInstance.json";
    public static final String URL_GET_INSTANCE_INFO = "/canal/getInstanceInfo.json";
    public static final String URL_UPDATE_POSITION = "/canal/updatePosition.json";
    public static final String URL_SYNC_RULES = "/syncRules";

    // ======================= 属性key 属性名称 ======================
    public static final String KEY_BIZ_NAME = "bizName";
    public static final String PRO_MANAGED_BY_CONSOLE = "managed.by.console";
    public static final String PRO_CANAL_INSTANCE_ALLOW_REPEAT = "canal.instance.allow.repeat";
    public static final String PRO_CANAL_SHIVA_HEARTBEAT_INTERVAL = "canal.shiva.heartbeat.interval";
    public static final String PRO_CONSOLE_ADDRESS = "console.address";
    public static final String PRO_SERVICE_NAME = "service.name";
    public static final String PRO_BIZ_NAME = "biz.name";
    public static final String PRO_CANAL_MODE = "canal.instance.global.mode";
    public static final String PRO_CANAL_MODE_MANAGER = "manager";
    public static final String PRO_RULE_SYNC_INTERVAL = "rule.sync.interval"; // 规则同步间隔，单位：秒
    public static final String PRO_RULE_PRINT_EVERYTIME = "rules.print.everytime"; // 是否每次都打印规则
    public static final int DERAULT_SCHEDULE_INTERVAL = 60; // 单位：秒

    // ======================= log name ============================
    public static final String LOG_NAME_HEARTBEAT = "HEARTBEAT";
    public static final String LOG_NAME_DEBUG = "DEBUG";
    public static final String LOG_NAME_ERROR = "ERROR";


    // ======================= other ============================
    public static final String CANAL_SERVER_ROLE_MASTER = "master";
    public static final String CANAL_SERVER_ROLE_STANDBY = "standby";

    public static final String CANAL_SERVER_STATE_RUNNING = "running";
    public static final String CHAR_FAILED = "failed";


}
