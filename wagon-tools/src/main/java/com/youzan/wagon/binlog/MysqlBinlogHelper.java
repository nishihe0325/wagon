package com.youzan.wagon.binlog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.inbound.SinkFunction;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection;
import com.alibaba.otter.canal.parse.inbound.mysql.dbsync.LogEventConvert;
import com.alibaba.otter.canal.parse.inbound.mysql.dbsync.TableMetaCache;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.taobao.tddl.dbsync.binlog.LogEvent;

public class MysqlBinlogHelper {

    /**
     * 注意，获取指定mysql实例，指定位点的CanalEntry.Entry。注意，该方法每调用一次都需要建立数据库连接，调用完后，关闭连接。
     * 不能频繁调用，对数据资源是一种损失。
     * 
     * @param host
     * @param port
     * @param username
     * @param password
     * @param journalName
     * @param position
     * @return
     * @throws Throwable
     */
    public static CanalEntry.Entry getSingleEntry(String host, int port, String username, String password, String journalName, long position) throws Throwable {
        MysqlConnection connection = null;
        MysqlConnection metaConnection = null;

        try {
            // 建立连接
            connection = buildConnection(host, port, username, password);

            // 初始化tableMetaCache
            metaConnection = connection.fork();
            try {
                metaConnection.connect();
            } catch (Exception e) {
                throw new CanalParseException(e);
            }
            TableMetaCache tableMetaCache = new TableMetaCache(metaConnection);

            // 初始化并启动binlogParser
            LogEventConvert binlogParser = new LogEventConvert();
            binlogParser.setCharset(Charset.forName("UTF-8"));
            binlogParser.setFilterQueryDcl(false);
            binlogParser.setFilterQueryDml(false);
            binlogParser.setFilterQueryDdl(false);
            binlogParser.setFilterTableError(false);
            binlogParser.start();
            binlogParser.setTableMetaCache(tableMetaCache);

            // 连接
            connection.connect();

            // 开始dump
            final CanalEntryHolder entryHolder = new CanalEntryHolder();
            connection.dump(journalName, position, new SinkFunction<LogEvent>() {
                public boolean sink(LogEvent event) {
                    try {
                        entryHolder.setEntry(binlogParser.parse(event));
                        if (entryHolder.getEntry() != null) {
                            return false; // 获取到第一条就退出
                        }
                        return true; // 获取到第一条就退出
                    } catch (Exception e) {
                        throw new CanalParseException(e);
                    }
                }
            });
            return entryHolder.getEntry();
        } catch (Throwable e) {
            throw e;
        } finally {
            closeMysqlConnection(connection);
            closeMysqlConnection(metaConnection);
        }
    }

    public static void closeMysqlConnection(MysqlConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (IOException e1) {
                // igonore
            }
        }
    }

    private static class CanalEntryHolder {
        private CanalEntry.Entry entry = null;

        public CanalEntry.Entry getEntry() {
            return entry;
        }

        public void setEntry(CanalEntry.Entry entry) {
            this.entry = entry;
        }

    }

    public static MysqlConnection buildConnection(String host, int port, String username, String password) {
        MysqlConnection connection = new MysqlConnection(new InetSocketAddress(host, port), username, password, (byte) 33, null);
        connection.getConnector().setReceiveBufferSize(64 * 1024);
        connection.getConnector().setSendBufferSize(64 * 1024);
        connection.getConnector().setSoTimeout(30 * 1000);
        connection.setCharset(Charset.forName("UTF-8"));
        connection.setSlaveId(3333);
        return connection;
    }

}
