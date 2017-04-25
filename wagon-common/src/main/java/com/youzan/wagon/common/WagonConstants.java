package com.youzan.wagon.common;

import java.text.SimpleDateFormat;

public final class WagonConstants {

    private WagonConstants() {
    }

    // ======================= 请求URL =============================
    public static final String URL_ROOT = "/";
    public static final String URL_CANAL_INSTANCE_LIST = "/canalInstanceList";
    public static final String URL_CANAL_INSTANCE_DETAIL = "/canalInstanceDetail";
    public static final String URL_REMOVE_INSTANCE = "/removeInstance";

    public static final String URL_RULE_BIZ_LIST = "/ruleBizList";
    public static final String URL_ADD_RULE_BIZ = "/addRuleBiz";
    public static final String URL_REMOVE_RULE_BIZ = "/removeRuleBiz";
    public static final String URL_UPDATE_RULE_BIZ_VERSION = "/updateRuleBizVersion";

    public static final String URL_RULE_LIST = "/ruleList";
    public static final String URL_NEW_RULE = "/newRule";
    public static final String URL_ADD_RULE = "/addRule";
    public static final String URL_EDIT_RULE = "/editRule";
    public static final String URL_UPDATE_RULE = "/updateRule";
    public static final String URL_RULE_DETAIL = "/ruleDetail";
    public static final String URL_DISABLE_RULE = "/disableRule";
    public static final String URL_ENABLE_RULE = "/enableRule";
    public static final String URL_REMOVE_RULE = "/removeRule";

    public static final String URL_ADD_OR_EDIT_FILED = "/addOrEditFiled";
    public static final String URL_REMOVE_RULE_FILED = "/removeRuleFiled";

    public static final String URL_ADD_OR_EDIT_OUT_FILED = "/addOrEditOutFiled";
    public static final String URL_REMOVE_OUT_FILED = "/removeOutFiled";

    public static final String URL_ADD_LOGIC_FILED = "/addLogicFiled";
    public static final String URL_REMOVE_LOGIC_FILED = "/removeLogicFiled";


    public static final String URL_GET_RULE_DATA = "getRuleData";

    public static final String URL_TOOLS = "/tools";
    public static final String URL_TOOLS_QUERY_TABLE = "/queryTable";
    public static final String URL_QUERY_BINLOG = "/queryBinlog";
    public static final String URL_TOOLS_TOOLS_QUERY_TABLE_POSITION = "/queryTablePosition";

    public static final String URL_CANAL_SERVER_LIST = "/canalServerList";
    public static final String URL_INSTANCE_MANAGE = "/instanceManage";
    public static final String URL_NEW_SERVICE = "/newService";
    public static final String URL_REMOVE_CANAL_INSTANCE = "/removeCanalInstance";
    public static final String URL_UPDAT_CANALINSTANCEVERSION = "/updatCanalInstanceVersion";
    public static final String URL_CLEAN_BINLOG_POSITION = "/cleanBinlogPosition";
    public static final String URL_NEW_CANAL_INSTANCE = "/newCanalInstance";
    public static final String URL_ADD_CANAL_INSTANCE = "/addCanalInstance";
    public static final String URL_EDIT_CANAL_INSTANCE = "/editCanalInstance";
    public static final String URL_UPDATE_CANAL_INSTANCE = "/updateCanalInstance";
    public static final String URL_SWITCH_CANAL_INSTANCE = "/switchCanalInstance";

    // ======================= VM 展示页 =============================
    public static final String VM_CANAL_INSTANCE_LIST = "canalInstanceList"; // 实例列表查看界面
    public static final String VM_CANAL_INSTANCE_DETAIL = "canalInstanceDetail"; // 实例详情查看界面

    public static final String VM_RULE_BIZ_LIST = "ruleBizList";
    public static final String VM_ADD_RULE_BIZ = "addRuleBiz";

    public static final String VM_RULE_LIST = "ruleList";
    public static final String VM_NEW_RULE = "newRule";
    public static final String VM_EDIT_RULE = "editRule";
    public static final String VM_RULE_DETAIL = "ruleDetail";

    public static final String VM_TOOLS = "tools";
    public static final String VM_QUERY_TABLE = "queryTable";
    public static final String VM_QUERY_BINLOG = "queryBinlog";

    public static final String VM_INSTANCE_MANAGE = "instanceManage"; // 实例列表查看界面
    public static final String VM_NEW_CANAL_INSTANCE = "newCanalInstance"; // 新增实例界面
    public static final String VM_EDIT_CANAL_INSTANCE = "editCanalInstance"; // 编辑实例界面

    public static final String VM_CANAL_SERVER_LIST = "canalServerList"; // 实例列表查看界面

    public static final String VM_SYS_ERR = "error"; // 异常页面

    // ======================= 请求参数名称 =============================
    public static final String KEY_BIZ_NAME = "bizName";
    public static final String KEY_CANAL_HOST_NAME = "canalHostName";
    public static final String KEY_CANAL_HOST = "canalHost";
    public static final String KEY_CANAL_PORT = "canalPort";
    public static final String KEY_DESTINATION = "destination";
    public static final String KEY_DB_HOST = "dbHost";
    public static final String KEY_DB_PORT = "dbPort";
    public static final String KEY_PAGE_SIZE = "pageSize"; // 每页最多显示条数

    public static final String KEY_ID = "id";
    public static final String KEY_TABLE_NAME = "tableName";
    public static final String KEY_TOPIC_NAME = "topicName";
    public static final String RULE_PARAM_EVENT_TYPE = "eventType";

    public static final short KEY_TOOLS_QUERY_TABLE_ACCURACY = 1; // 完全匹配
    public static final short KEY_TOOLS_QUERY_TABLE_VAGUE = 2;// 模糊查询

    public static final short KEY_INSTANCE_STATUS_RUNNING = 1;// 运行中
    public static final short KEY_INSTANCE_STATUS_PAUSE = 2;// 暂停
    public static final short KEY_INSTANCE_STATUS_SUSPEND = 3;// 挂起


    public static final String KEY_SERVICE_NAME = "serviceName";
    public static final String KEY_RULE_BIZ_NAME = "ruleBizName";

    // ======================= 属性key 属性名称 =============================
    public static final String PRO_EXPIRE_JOB_LOG_DAYS = "expire.job.log.days"; // 单位：天
    public static final String PRO_INTERVAL_DELETE_JOB_LOG = "delete.job.log.interval.minutes"; // 单位：分钟
    public static final String PRO_INTERVAL_UPDATE_FROM_CMDB = "update.from.cmdb.minutes"; // 单位：分钟
    public static final String PRO_INTERVAL_UPDATE_FROM_OA = "update.from.oa.minutes"; // 单位：分钟
    public static final String PRO_CMDB_ADDR = "cmdb.addr"; // cmdb地址
    public static final String PRO_CMDB_TOKEN = "cmdb.token";

    // ======================= MODEL 属性名称 =============================
    public static final String MODEL_SERVER_INSTANCE_MAP = "serverInstanceMap";
    public static final String MODEL_INSTANCE_INFO = "instanceInfo";

    public static final String MODEL_RULE_BIZ_INFOS = "ruleBizInfos";
    public static final String MODEL_RULE_BIZ_INFO = "ruleBizInfo";

    public static final String MODEL_RULE_INFOS = "ruleInfos";
    public static final String MODEL_RULE_INFO = "ruleInfo";

    public static final String MODEL_SELECTED_EVENT_TYPES = "selectedEventTypes";

    public static final String MODEL_NO_SELECTED_EVENT_TYPES = "noSelectedEventTypes";

    public static final String MODEL_SHOW_FIELD_ROW = "showFieldRow";
    public static final String MODEL_SHOW_OUT_FIELD_ROW = "showOutFieldRow";
    public static final String MODEL_SHOW_LOGIC_FIELD_ROW = "showLogicFieldRow";
    public static final String MODEL_SHOW_CONDITION_OR_OUT_FIELD_RELA = "showConditionOrOutFieldRela";
    public static final String MODEL_SHOW_OTHER_ATTR = "showOtherAttr";
    public static final String MODEL_RULE_FIELD_INFOS = "ruleFieldInfos";
    public static final String MODEL_RULE_OUT_FIELD_INFOS = "ruleOutFieldInfos";
    public static final String MODEL_RULE_LOGIC_FIELD_INFOS = "ruleLogicFieldInfos";

    public static final String MODEL_RESULT_MSG = "resultMsg";

    public static final String MODEL_SERVER_LIST = "serverlist";

    public static final String MODEL_SERVICE_NAME = "serviceName";
    public static final String MODEL_ALL_RULE_BIZ_NAMES = "allRuleBizNames";
    public static final String MODEL_ALL_CANAL_SERVICES = "allCanalServices";
    public static final String MODEL_CUR_CANAL_SERVICE = "curCanalService";
    public static final String MODEL_CUR_CANAL_INSTANCE = "curCanalInstance";
    public static final String MODEL_CUR_ALL_CANAL_INSTANCES = "curAllCanalInstances";
    public static final String MODEL_CUR_ALL_CANAL_SERVERS = "curAllCanalServers";

    // ======================= MODEL 属性值 =============================
    public static final String MODEL_VALUE_TRUE = "true";
    public static final String MODEL_VALUE_FALSE = "false";

    // 事件类型名称
    public static final String EVENT_TYPE_INSERT = "INSERT";
    public static final String EVENT_TYPE_UPDATE = "UPDATE";
    public static final String EVENT_TYPE_DELETE = "DELETE";

    // field是更新前的值还是更新后的值
    public static final String FIELD_POSITION_AFTER = "after";
    public static final String FIELD_POSITION_BEFORE = "before";

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // Subscribe
    public static final String SUBSCRIBE_ALL = ".*\\..*";

    // Properties key
    public static final String KEY_WAGON_CONSOLE_ADDR = "wagon.console.address";
    public static final String KEY_WAGON_CONSOLE_PATH = "wagon.console.webapp.path";
    public static final String KEY_WAGON_CONSOLE_PORT = "wagon.console.webapp.port";

    // 默认控制台地址，参数"wagon.console.address"未设置时，会使用该地址
    public static final String DEFAULT_WAGON_CONSOLE_ADDR = "http://wagon.qima-inc.com";

    // period
    public static final String PERIOD_CONSUMER_SCAN = "consumer.scan.period";
    public static final String PERIOD_UPDATE_RULE_FROM_CONSOLE = "update.rule.from.console.period";
    public static final String PERIOD_PRINT_RULE_INFO = "print.rule.period";

    // ======================= 属性key 属性名称 =============================
    public static final String PRO_HEARTBEAT_INTERVAL = "heartbeat.interval"; // 心跳间隔，单位：秒
    public static final String PRO_UPDATE_RULE_MANAGER_INTERVAL = "update.rule.manager.interval"; // 更新规则间隔，单位：分钟
    public static final String PRO_UPDATE_BIZTABLE_INTERVAL = "update.biztable.interval"; // 更新业务表名称间隔，单位：分钟

    public static final String PRO_PROCESSOR_TYPE = "entry.processor.type"; // 处理器类型,推送到nsq还是打印到日志
    public static final String PRO_PROCESSOR_TYPE_NSQ = "nsq"; // 推送到nsq
    public static final String PRO_PROCESSOR_TYPE_LOG = "log"; // 打印到日志

    public static final String PRO_NSQ_ADDRESS = "nsq.address"; // nsq 地址
    public static final String PRO_NSQ_TIMEOUT = "nsq.timeout"; // nsq 连接超时时间
    public static final String PRO_NSQ_RETRY_COUNT = "nsq.retry.count";
    public static final String PRO_NSQ_THREAD_POOL_4IO = "nsq.thread.pool.4io"; //
    public static final String PRO_NSQ_CONNECTION_POOL_SIZE = "nsq.connection.pool.size"; //
    public static final String PRO_NSQ_DCCURLS = "nsq.dcc.urls";
    public static final String PRO_NSQ_DCC_BACKUPPATH = "nsq.dcc.backupPath";
    public static final String PRO_NSQ_SDK_ENV = "nsq.sdk.env";
    public static final String PRO_NSQ_PUB_RETRY_INTERVAL = "nsq.pub.retry.interval";

    // ======================= log name =============================
    public static final String LOG_NAME_HEARTBEAT = "HEARTBEAT";
    public static final String LOG_NAME_DEBUG = "DEBUG";
    public static final String LOG_NAME_ERROR = "ERROR";
    public static final String LOG_NAME_NSQ = "NSQ";
    public static final String LOG_NAME_NSQ_FAIL = "NSQ_FAIL";
    public static final String LOG_NAME_STORE = "STORE";

    public static final int DERAULT_INTERVAL_UPDATE_FROM_CMDB = 10; // 单位：分钟
    public static final int DERAULT_INTERVAL_UPDATE_FROM_OA = 10; // 单位：分钟

    // ======================= derault =============================
    public static final int DERAULT_PAGE_SIZE = 1000; // 默认显示条数

    public static final int DEFAULT_NSQ_CONN_TIMEOUT = 3 * 1000; // 默认NSQ连接超时时间，单位 ms
    public static final int DEFAULT_NSQ_THREAD_POOLSIZE_4IO = 1; // 默认网络处理线程数
    public static final int DEFAULT_NSQ_CONNECTION_POOL_SIZE = 32; // 默认NSQ连接池大小
    public static final int DEFAULT_NSQ_DEFAULT_MAX_RETRY_INTERVAL = 60; // 默认最大重试间隔


    // ======================= rule logic operator =============================
    public static final short RULE_ENABLE_STATE_ENABLED = 1; // 规则状态，启用中
    public static final short RULE_ENABLE_STATE_DISABLED = 2; // 规则状态，禁用中

    public static final short RULE_PUSH_DATA_TYPE_ALL = 1; // 更新前后同时推送
    public static final short RULE_PUSH_DATA_TYPE_SIMPLE = 2; // 只推送更新后，或前(delete);

    public static final short RULE_FIELD_CONDITION_AND = 1; // and;
    public static final short RULE_FIELD_CONDITION_OR = 2; // or

    public static final short RULE_OUT_TYPE_IGNORE = 1; // 忽略
    public static final short RULE_OUT_TYPE_CONTAIN = 2; // 只包含

    public static final short RULE_FIELD_POSITION_AFTER = 1; // 更新后
    public static final short RULE_FIELD_POSITION_BEFORE = 2; // 更新前

    public static final String TAB_NAME = "tabName";

    public static final String RULE_LOGIC_OPERATOR_NEW = "new"; // 新加一个字段
    public static final String RULE_LOGIC_OPERATOR_FIX = "fix"; // 固定一个值
    public static final String RULE_LOGIC_OPERATOR_PLUS = "plus"; // 加
    public static final String RULE_LOGIC_OPERATOR_SUBTRACT = "subtract"; // 减

    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat DATE_TIME_FORMAT_PURE = new SimpleDateFormat("yyyyMMddHHmmss");

    // ======================= other =============================
    public static final String SERVICE_NAME = "serviceName";
    public static final String TAB_NAME_INSTANCE_MANAGE = "instanceManage";
    public static final String TAB_NAME_INSTANCE_LIST = "canalInstanceList";

}
