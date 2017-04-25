package com.youzan.wagon.persistent.dao;

import java.net.InetSocketAddress;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchRequestData;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchRequestData.DBData;

/**
 * @author wangguofeng since 2016年1月26日 下午7:09:12
 */
public class CanalDBSwitchTest {

    public static void main(String[] args) throws Exception {
        // 初始化http post请求体(json字符串)
        DBData fromDBData = new DBData(); // 切换前数据库信息
        fromDBData.setHost("kdt-qa4");
        fromDBData.setPort(3301);
        fromDBData.setUsername("cobar");
        fromDBData.setPassword("cobar");

        DBData toDBData = new DBData(); // 切换后数据库信息
        toDBData.setHost("kdt-qa4");
        toDBData.setPort(3302);
        toDBData.setUsername("cobar");
        toDBData.setPassword("cobar");

        String json = new CanalDBSwitchRequestData(fromDBData, toDBData).toJson();

        // 发送请求
        // String resp =
        // HttpClientUtil.httpRequestByPost("http://192.168.66.207:8008/canal/canalDBSwitch.json",
        // json);

        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("127.0.0.1", 11111), "", "", "");
        if (connector instanceof SimpleCanalConnector) {
            ((SimpleCanalConnector) connector).setRollbackOnConnect(false); // 设置为不指定具体的destination
        }
        connector.connect(); // 建立连接
        connector.canalDBSwitch(null);

        // 打印结果(json字符串)
        // System.out.println(resp);
    }

}
