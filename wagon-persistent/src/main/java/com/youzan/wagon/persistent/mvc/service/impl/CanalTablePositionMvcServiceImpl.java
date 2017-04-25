package com.youzan.wagon.persistent.mvc.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.extend.common.bean.TablePositionData;
import com.alibaba.otter.canal.extend.common.bean.TablePositionDatas;
import com.youzan.wagon.persistent.CanalConnectorManager;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.model.TablePositionBean;
import com.youzan.wagon.persistent.mvc.service.CanalTablePositionMvcService;
import com.youzan.wagon.persistent.service.CanalServerInfoService;

@Component("canalTablePositionMvcService")
public class CanalTablePositionMvcServiceImpl implements CanalTablePositionMvcService {

    private static final Logger LOG = LoggerFactory.getLogger(CanalTablePositionMvcServiceImpl.class);

    @Resource()
    private CanalServerInfoService canalServerInfoService;

    /**
     * 查询指定表名所在的mysql地址信息
     * 
     * @param tableName
     * @return
     */
    public String queryTablePosition(String tableName) {
        TablePositionDatas postions = new TablePositionDatas();
        List<CanalServerInfo> servers = canalServerInfoService.findAll();

        if (CollectionUtils.isEmpty(servers)) {
            return "no tablse";
        }

        // 向所有server发送查询请求
        for (CanalServerInfo server : servers) {
            queryTablePosition(postions, server, tableName);
        }

        return postions.toJson();
    }

    public List<TablePositionBean> queryTablePositionDirectory(String tableName, String queryType, String showSchemaName) {
        List<TablePositionBean> positions = new ArrayList<TablePositionBean>();

        String root = getClass().getResource("/").getFile();
        File dir = new File(root + "canalinstances");
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });

        for (File file : files) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (StringUtils.isBlank(line)) {
                        continue;
                    }

                    String[] array = line.split(" ");
                    String canalHost = array[0].trim();
                    String canalPort = array[1].trim();
                    String canalHostName = array[2].trim();
                    String destination = array[3].trim();
                    String dbAddress = array[4].trim();
                    String username = array[5].trim();
                    String password = array[6].trim();
                    String bizName = array[7].trim();

                    TablePositionBean queryBean = new TablePositionBean(canalHost, Integer.valueOf(canalPort), canalHostName, destination, bizName, dbAddress);
                    queryBean.setUsername(username);
                    queryBean.setPassword(password);
                    queryTablePositionDirectory(queryBean, tableName, positions, queryType, showSchemaName);
                }
                reader.close();
            } catch (IOException e) {
                LOG.error("## Something goes wrong when reading file:\n{}", ExceptionUtils.getFullStackTrace(e));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }

        return positions;
    }

    // ================== helper method ===================
    private void queryTablePosition(TablePositionDatas postions, CanalServerInfo server, String tableName) {
        CanalConnector canalConnector = null;
        for (int times = 1; times <= 3; times++) { // 重试3次
            InetSocketAddress canalAddress = new InetSocketAddress(server.getCanalHost(), server.getCanalPort());
            try {
                canalConnector = CanalConnectorManager.getConnector(canalAddress);
                TablePositionData data = canalConnector.queryTablePosition(tableName);
                if (data != null) {
                    postions.add(data);
                }
            } catch (Exception e) {
                if (times < 3) {
                    CanalConnectorManager.remove(canalAddress); // 删除，并重建连接
                    LOG.error("send queryTablePosition failed, times={}", times);
                } else {
                    LOG.error("send queryTablePosition failed: ", e);
                }
            }

        }
    }

    private List<TablePositionBean> queryTablePositionDirectory(TablePositionBean queryBean, String tableName, List<TablePositionBean> positions, String queryType, String showSchemaName) {
        String url = "jdbc:mysql://" + queryBean.getDbAddress() + "/information_schema?useServerPrepStmts=true";

        // sql
        StringBuilder builder = new StringBuilder();
        if ("on".equals(showSchemaName)) {
            builder.append("SELECT table_schema, table_name FROM tables where table_name ");
            if ("1".equals(queryType)) {
                builder.append("='").append(tableName).append("'");
            } else {
                builder.append("like '%").append(tableName).append("%'");
            }
        } else {
            builder.append("SELECT table_name FROM tables where table_name ");
            if ("1".equals(queryType)) {
                builder.append("='").append(tableName).append("' ");
            } else {
                builder.append("like '%").append(tableName).append("%' ");
            }
            builder.append("group by table_name");
        }
        String sql = builder.toString();

        // query
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, queryBean.getUsername(), queryBean.getPassword());
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                TablePositionBean bean = queryBean.clone();
                bean.setTableName(rs.getString("table_name"));
                if ("on".equals(showSchemaName)) {
                    bean.setTableSchema(rs.getString("table_schema"));
                }
                positions.add(bean);
            }
        } catch (Exception e) {
            LOG.error("## Something goes wrong when quering table:\n{}", ExceptionUtils.getFullStackTrace(e));
            TablePositionBean bean = queryBean.clone();
            bean.setRemark("query failed.");
            positions.add(bean);
        } finally {
            release(conn, statement, rs);
        }

        return positions;
    }

    private void release(Connection conn, Statement statement, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

}
