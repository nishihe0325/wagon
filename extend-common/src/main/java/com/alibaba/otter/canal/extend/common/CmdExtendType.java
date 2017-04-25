package com.alibaba.otter.canal.extend.common;

/**
 * @author wangguofeng since 2015年12月31日 下午4:03:38
 */
public class CmdExtendType {

    // mysql主备切换命令
    public static final String CANAL_DB_SWITCH = "canal_db_switch";
    public static final String CANAL_DB_SWITCH_RESULT = "canal_db_switch_result";

    // 查询canal实例运行时信息命令
    public static final String QUERY_CANAL_RUNNING_INFO = "query_canal_running_info";
    public static final String QUERY_CANAL_RUNNING_INFO_RESULT = "query_canal_running_info_result";

    // 查询canal实例运行时信息命令
    public static final String QUERY_TABLE_POSITION = "query_table_position";
    public static final String QUERY_TABLE_POSITION_RESULT = "query_table_position_result";

}
