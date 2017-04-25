package com.youzan.wagon.console.controller.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.youzan.wagon.binlog.CanalServerBinlogParser;
import com.youzan.wagon.binlog.MysqlBinlogParser;
import com.youzan.wagon.binlog.handler.DefaultEntryHandler;
import com.youzan.wagon.persistent.model.CanalInstanceInfo;
import com.youzan.wagon.persistent.mvc.service.CanalTablePositionMvcService;
import com.youzan.wagon.persistent.service.CanalInstanceInfoService;
import com.youzan.wagon.persistent.service.CanalServerInfoService;

/**
 * 查询表所在的mysql地址MVC控制类
 * 
 * @author wangguofeng since 2016年3月9日 下午2:40:59
 */
@Controller
public class ToolsController {

    private static final Logger LOG = LoggerFactory.getLogger(ToolsController.class);
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // MVC URL
    private static final String CANAL_TOOL_PAGE_URL = "/tools";

    private static final String QUERY_TABLE_URL = "/queryTable";
    private static final String QUERY_BINLOG_URL = "/queryBinlog";
    private static final String QUERY_TABLE_POSITION_URL = "/queryTablePosition";

    // VM name
    private static final String VM_CANAL_TOOL = "tools";
    private static final String VM_QUERY_TABLE = "queryTable";
    private static final String VM_QUERY_BINLOG = "queryBinlog";

    @Resource
    private CanalTablePositionMvcService canalTablePositionMvcService;

    @Resource()
    private CanalServerInfoService canalServerInfoService;
    @Resource()
    private CanalInstanceInfoService canalInstanceInfoService;

    @RequestMapping(value = CANAL_TOOL_PAGE_URL, method = RequestMethod.GET)
    public String tools(Model model, String tableName) {
        return VM_CANAL_TOOL;
    }

    @RequestMapping(value = QUERY_TABLE_URL, method = RequestMethod.GET)
    public String queryTable(Model model, String tableName) {
        return VM_QUERY_TABLE;
    }

    @RequestMapping(value = QUERY_TABLE_POSITION_URL, method = RequestMethod.POST)
    public String queryTablePosition(Model model, String tableName) {
        String resultMsg = null;
        try {
            String tableAddressList = canalTablePositionMvcService.queryTablePosition(tableName);
            model.addAttribute("tableAddressList", tableAddressList);
        } catch (Exception e) {
            LOG.error("## Something goes wrong when handling queryTablePosition request:\n{}", ExceptionUtils.getFullStackTrace(e));
            resultMsg = "queryTablePosition failed!";
        }
        // "".length()
        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_TOOL;
    }

    @RequestMapping(value = QUERY_BINLOG_URL, method = RequestMethod.GET)
    public String queryBinlog(Model model) {
        String resultMsg = null;

        try {
            model.addAttribute("canalServers", canalServerInfoService.findAll());
        } catch (Exception e) {
            resultMsg = "查询canal实例失败!";
            LOG.error("QueryBinlog failed: {}", ExceptionUtils.getFullStackTrace(e));
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_QUERY_BINLOG;
    }

    @RequestMapping(value = QUERY_BINLOG_URL, method = RequestMethod.POST)
    @ResponseBody
    public String queryBinlogByPost(Model model, //
            String canalAddr, //
            String dbName, //
            String tableName, //
            String startTime, //
            String endTime, //
            String columnCondition) {
        StringBuilder resp = new StringBuilder();

        try {
            if (StringUtils.isBlank(canalAddr)) {
                resp.append("请选择canal服务器");
            } else if (StringUtils.isBlank(tableName)) {
                resp.append("请选择输入表名");
            } else if (StringUtils.isBlank(columnCondition)) {
                resp.append("请选择输入查询条件");
            } else {
                String[] array = canalAddr.split(":");
                String canalHost = array[0];
                String canalPort = array[1];

                String[] conditions = columnCondition.split(",");
                ConditionPair[] conditionArray = new ConditionPair[conditions.length];
                for (int i = 0; i < conditions.length; i++) {
                    String condition = conditions[i];
                    String[] array2 = condition.split("=");
                    conditionArray[i] = new ConditionPair(array2[0], array2[1]);
                }

                Long startTimestamp = DATE_FORMAT.parse(startTime).getTime();
                Long endTimestamp = DATE_FORMAT.parse(endTime).getTime();

                List<CanalInstanceInfo> instances = canalInstanceInfoService.findByHostAndPort(canalHost, Integer.valueOf(canalPort));

                // 生成实例查询器
                List<MysqlBinlogParser> parsers = new ArrayList<MysqlBinlogParser>();
                CountDownLatch countDownLatch = new CountDownLatch(instances.size());
                if (instances != null && instances.size() > 0) {
                    for (CanalInstanceInfo instance : instances) {
                        // 设置handler
                        DefaultEntryHandler handler = new DefaultEntryHandler();
                        handler.setSchemaName(dbName);
                        handler.setTableName(tableName);
                        for (ConditionPair pair : conditionArray) {
                            handler.addColumn(pair.getColumnName(), pair.getColumnValue());
                        }

                        // 添加MysqlBinlogParser
                        // mD9KJ9UzfERpxadc
                        MysqlBinlogParser parser = new MysqlBinlogParser(instance.getDbHost(), instance.getDbPort(), instance.getDbUsername(), "cobar", countDownLatch, handler);
                        parser.setStartTimestamp(startTimestamp);
                        parser.setEndTimestamp(endTimestamp);
                        parsers.add(parser);
                    }

                    return new CanalServerBinlogParser().parse(parsers, countDownLatch);
                }
            }
        } catch (Exception e) {
            LOG.error("QueryBinlog failed: {}", ExceptionUtils.getFullStackTrace(e));
            resp.append("查询失败");
        }

        try {
            if (StringUtils.isBlank(canalAddr)) {
                resp.append("请选择canal服务器");
            } else if (StringUtils.isBlank(tableName)) {
                resp.append("请选择输入表名");
            } else if (StringUtils.isBlank(columnCondition)) {
                resp.append("请选择输入查询条件");
            } else {
                String[] array = canalAddr.split(":");
                String canalHost = array[0];
                String canalPort = array[1];

                String[] conditions = columnCondition.split(",");
                ConditionPair[] conditionArray = new ConditionPair[conditions.length];
                for (int i = 0; i < conditions.length; i++) {
                    String condition = conditions[i];
                    String[] array2 = condition.split("=");
                    conditionArray[i] = new ConditionPair(array2[0], array2[1]);
                }

                Long startTimestamp = DATE_FORMAT.parse(startTime).getTime();
                Long endTimestamp = DATE_FORMAT.parse(endTime).getTime();

                List<CanalInstanceInfo> instances = canalInstanceInfoService.findByHostAndPort(canalHost, Integer.valueOf(canalPort));

                // 生成实例查询器
                List<MysqlBinlogParser> parsers = new ArrayList<MysqlBinlogParser>();
                CountDownLatch countDownLatch = new CountDownLatch(instances.size());
                if (instances != null && instances.size() > 0) {
                    for (CanalInstanceInfo instance : instances) {
                        // 设置handler
                        DefaultEntryHandler handler = new DefaultEntryHandler();
                        handler.setSchemaName(dbName);
                        handler.setTableName(tableName);
                        for (ConditionPair pair : conditionArray) {
                            handler.addColumn(pair.getColumnName(), pair.getColumnValue());
                        }

                        // 添加MysqlBinlogParser
                        // mD9KJ9UzfERpxadc
                        MysqlBinlogParser parser = new MysqlBinlogParser(instance.getDbHost(), instance.getDbPort(), instance.getDbUsername(), "cobar", countDownLatch, handler);
                        parser.setStartTimestamp(startTimestamp);
                        parser.setEndTimestamp(endTimestamp);
                        parsers.add(parser);
                    }

                    return new CanalServerBinlogParser().parse(parsers, countDownLatch);
                }
            }
        } catch (Exception e) {
            LOG.error("QueryBinlog failed: {}", ExceptionUtils.getFullStackTrace(e));
            resp.append("查询失败");
        }

        return resp.toString();
    }

    private static class ConditionPair {
        private String columnName;
        private String columnValue;

        public ConditionPair(String columnName, String columnValue) {
            this.columnName = columnName;
            this.columnValue = columnValue;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getColumnValue() {
            return columnValue;
        }

    }

}
