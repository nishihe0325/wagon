package com.youzan.wagon.binlog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.youzan.wagon.binlog.config.ConnectionConfig;
import com.youzan.wagon.binlog.config.FilterConditionConfig;
import com.youzan.wagon.binlog.handler.DefaultEntryHandler;
import com.youzan.wagon.binlog.handler.DefaultEntryHandler.RowResult;

/**
 * @author wangguofeng since 2016年5月7日 下午5:55:30
 */
public class MysqlBinlogParserLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlBinlogParserLauncher.class);
    private static final Logger LOG_RESULT = LoggerFactory.getLogger("RESULT");

    public static void main(String[] args) throws ParseException, IOException {
        try {
            FilterConditionConfig conditionConfig = new FilterConditionConfig();
            List<ConnectionConfig> connConfigs = new ArrayList<ConnectionConfig>();

            // 添加配置(过滤，连接)
            fillConfig(conditionConfig, connConfigs);

            // 检测配置信息
            checkFilterConditionConfig(conditionConfig);
            checkConnectionConfig(connConfigs);

            // 生成实例查询器
            List<MysqlBinlogParser> parsers = new ArrayList<MysqlBinlogParser>();
            CountDownLatch countDownLatch = new CountDownLatch(connConfigs.size());
            for (ConnectionConfig connConfig : connConfigs) {
                DefaultEntryHandler handler = new DefaultEntryHandler();
                configEntryHandler(handler, conditionConfig);

                MysqlBinlogParser parser = new MysqlBinlogParser(connConfig, countDownLatch, handler);
                parsers.add(parser);
            }
            printMysqlBinlogParser(parsers); // 打印下信息

            // 启动实例查询器线程(一个查询器一个线程)
            LOG.info("starting parser...");
            for (MysqlBinlogParser parser : parsers) {
                LOG.info("starting parser {} ...", parser.toString());
                new Thread(parser).start();
            }

            // 等待所有mysql实例查询完
            try {
                LOG.info("waiting all parser complete...\n\n\n");
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOG.error(ExceptionUtils.getFullStackTrace(e));
            }
            LOG.info("all parser completed.\n\n\n");

            // 处理查询结果
            handQueryResult(parsers);
        } catch (Exception e) {
            LOG.error("Something goes wrong:\n{}", ExceptionUtils.getFullStackTrace(e));
        }
    }

    private static void fillConfig(FilterConditionConfig conditionConfig, List<ConnectionConfig> connConfigs) throws ParseException, IOException {
        String root = MysqlBinlogParserLauncher.class.getResource("/").getFile();
        File file = new File(root + "sync.properties");
        BufferedReader reader = null;
        String line = null;

        if (file == null || !file.exists()) {
            throw new CanalParseException(String.format("file %s not exist.", file.getAbsolutePath()));
        }

        try {
            reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isBlank(line = line.trim())) {
                    continue;
                }

                if (line.startsWith("condition")) {
                    fillFilterConditionConfig(line, conditionConfig); // 设置条件
                } else if (line.startsWith("connettion")) {
                    fillConnectionConfig(line, connConfigs); // 设置连接信息
                }
            }
        } catch (IOException e) {
            LOG.error("parse file failed, {}, cause by:\n{}", file.getAbsolutePath(), ExceptionUtils.getFullStackTrace(e));
            throw new CanalParseException(ExceptionUtils.getFullStackTrace(e)); // 抛给上层
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void fillFilterConditionConfig(String line, FilterConditionConfig config) {
        String condition = line.split(" ")[1].trim();
        String[] array = condition.split("=");
        String key = array[0].trim();
        String value = array[1].trim();

        if ("tableName".equals(key)) {
            config.setTableName(value);
        } else if ("schemaName".equals(key)) {
            config.setSchemaName(value);
        } else {
            config.addColumn(key, value);
        }
    }

    private static void fillConnectionConfig(String line, List<ConnectionConfig> configs) throws ParseException {
        String[] array = line.split(" ");
        String ip = array[1].trim();
        String port = array[2].trim();
        String username = array[3].trim();
        String password = array[4].trim();
        String startPosition = array[5].trim();
        String endPosition = array[6].trim();

        ConnectionConfig config = new ConnectionConfig(ip, Integer.valueOf(port), username, password);
        config.setStartPosition(startPosition);
        config.setEndPosition(endPosition);

        configs.add(config);
    }

    private static void checkFilterConditionConfig(FilterConditionConfig config) {
        if (StringUtils.isBlank(config.getTableName())) {
            throw new CanalParseException("tableName must be not blank.");
        }
        Map<String, String> columns = config.getColumns();
        if (CollectionUtils.isEmpty(columns)) {
            throw new CanalParseException("columns must be not empty.");
        }

        for (String key : columns.keySet()) {
            if (StringUtils.isBlank(key)) {
                throw new CanalParseException(String.format("column key is blank, %s=%s", key, columns.get(key)));
            }
            if (StringUtils.isBlank(columns.get(key))) {
                throw new CanalParseException(String.format("column value is blank, %s=%s", key, columns.get(key)));
            }
        }
    }

    private static void checkConnectionConfig(List<ConnectionConfig> connConfigs) {
        if (CollectionUtils.isEmpty(connConfigs)) {
            throw new CanalParseException("connection configs must be not empty.");
        }

        for (ConnectionConfig config : connConfigs) {
            if (config.getStartPosition() == null) {
                throw new CanalParseException("connection start positin is blank.");
            }
            if (config.getEndPosition() == null) {
                throw new CanalParseException(String.format("connection end positin is blank."));
            }
        }
    }

    private static void configEntryHandler(DefaultEntryHandler handler, FilterConditionConfig config) {
        handler.setSchemaName(config.getSchemaName());
        handler.setTableName(config.getTableName());

        Map<String, String> columns = config.getColumns();
        for (String columnName : columns.keySet()) {
            handler.addColumn(columnName, columns.get(columnName));
        }
    }

    private static void printMysqlBinlogParser(List<MysqlBinlogParser> parsers) {
        StringBuilder conditionConfig = null;
        StringBuilder connConfig = new StringBuilder();
        for (MysqlBinlogParser parser : parsers) {
            if (conditionConfig == null) {
                DefaultEntryHandler handler = (DefaultEntryHandler) parser.getEntryHandler();
                conditionConfig = new StringBuilder();
                conditionConfig.append("\nschemaName=").append(handler.getSchemaName());
                conditionConfig.append("; tableName=").append(handler.getTableName());
                Map<String, String> columns = handler.getColumns();
                for (String key : columns.keySet()) {
                    conditionConfig.append("\n").append(key).append("=").append(columns.get(key));
                }
            }

            connConfig.append("\n").append(parser.toString());
        }

        LOG.info("\n\n\n\nquery condition:{} \nquery connections:{}\n\n\n", conditionConfig.toString(), connConfig.toString());
    }

    private static void handQueryResult(List<MysqlBinlogParser> parsers) {
        if (parsers.isEmpty()) {
            LOG_RESULT.info("there has no result.");
        } else {
            LOG_RESULT.info("==================================result==================================");
            for (MysqlBinlogParser parser : parsers) {
                DefaultEntryHandler handler = (DefaultEntryHandler) parser.getEntryHandler();
                List<RowResult> result = handler.getResults();
                for (RowResult rowResult : result) {
                    String schemaName = rowResult.getSchemaName();
                    String eventType = rowResult.getEventType();
                    String dateFormat = rowResult.getDateFormat();
                    RowData rowData = rowResult.getRowData();

                    Map<String, Object> before = RowDataUtils.feedMap(rowData.getBeforeColumnsList());
                    Map<String, Object> after = RowDataUtils.feedMap(rowData.getAfterColumnsList());

                    StringBuilder info = new StringBuilder();
                    info.append(String.format("schemaName:%s, type:%s, timestamp %s", schemaName, eventType, dateFormat));
                    info.append("\nbefore:").append(JSON.toJSONString(before));
                    info.append("\nafter:").append(JSON.toJSONString(after));
                    LOG_RESULT.info("\n\n\n================================================================================");
                    LOG_RESULT.info(info.toString());
                }
            }
        }
    }

}
