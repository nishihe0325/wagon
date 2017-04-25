package com.youzan.wagon.binlog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.youzan.wagon.binlog.handler.DefaultEntryHandler;
import com.youzan.wagon.binlog.handler.DefaultEntryHandler.RowResult;

public class CanalServerBinlogParser {

    private static final Logger LOG = LoggerFactory.getLogger(CanalServerBinlogParser.class);

    public String parse(List<MysqlBinlogParser> parsers, CountDownLatch countDownLatch) {
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

        // 返回解析结果
        StringBuilder buffer = new StringBuilder();
        buffer.append("==================================result==================================");

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
                buffer.append("\n\n\n================================================================================");
                buffer.append(info.toString());
            }
        }

        return buffer.toString();
    }

}
