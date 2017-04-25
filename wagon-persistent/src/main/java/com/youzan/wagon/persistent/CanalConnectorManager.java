package com.youzan.wagon.persistent;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;

/**
 * shiva到canal服务器的连接管理器
 * 
 * @author wangguofeng since 2016年3月14日 上午10:15:06
 */
public class CanalConnectorManager {

    private static final Logger LOG = LoggerFactory.getLogger(CanalConnectorManager.class);

    private static Map<InetSocketAddress, CanalConnector> connectorMap = new HashMap<InetSocketAddress, CanalConnector>();

    public static synchronized CanalConnector getConnector(String canalHost, Integer canalPort) {
        return connectorMap.get(new InetSocketAddress(canalHost, canalPort));
    }

    public static synchronized CanalConnector getConnector(InetSocketAddress canalAddr) throws CanalClientException {
        CanalConnector connector = connectorMap.get(canalAddr);
        if (connector == null) {
            connector = CanalConnectors.newSingleConnector(canalAddr, null, null, null);
            if (connector instanceof SimpleCanalConnector) {
                ((SimpleCanalConnector) connector).setRollbackOnConnect(false); // 设置为不指定具体的destination
                ((SimpleCanalConnector) connector).setSoTimeout(3000); // 重新设置为3妙
            } else {
                ((ClusterCanalConnector) connector).setSoTimeout(3000); // 重新设置为3妙
            }

            long start = System.currentTimeMillis();
            try {
                connector.connect(); // 建立连接
            } catch (Exception e) {
                LOG.error("connect cost {}ms, remote:{}", (System.currentTimeMillis() - start), canalAddr);
                throw e;
            }

            LOG.info("connect cost {}ms, remote:{}", (System.currentTimeMillis() - start), canalAddr);
            connectorMap.put(canalAddr, connector);
            return connector;
        }

        return connector;
    }

    public static synchronized void remove(InetSocketAddress canalAddress) {
        Map<InetSocketAddress, CanalConnector> map = new HashMap<InetSocketAddress, CanalConnector>(connectorMap);
        CanalConnector connector = map.get(canalAddress);
        if (connector != null && connector instanceof SimpleCanalConnector) {
            connector.disconnect();
            connectorMap.remove(canalAddress);
        }
    }
}
