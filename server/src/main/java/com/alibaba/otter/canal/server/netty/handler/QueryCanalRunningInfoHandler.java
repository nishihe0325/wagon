package com.alibaba.otter.canal.server.netty.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.extend.common.bean.CanalInstanceRunningData;
import com.alibaba.otter.canal.extend.common.bean.CanalServerRunningData;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.meta.CanalMetaManager;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty.CanalServerWithNetty;
import com.alibaba.otter.canal.store.memory.MemoryEventStoreWithBuffer;

public class QueryCanalRunningInfoHandler {

    private static final Logger logger = LoggerFactory.getLogger(QueryCanalRunningInfoHandler.class);

    private CanalServerWithEmbedded embeddedServer;
    private String hostName;
    private String canalHost;

    public QueryCanalRunningInfoHandler(CanalServerWithEmbedded embeddedServer) {
        this.embeddedServer = embeddedServer;
        this.hostName = AddressUtils.getHostName();
        canalHost = AddressUtils.getHostIp();
    }

    /**
     * 查询指定destination实例运行信息，如果destination为空，则查询全部
     * 
     * @param destination
     *            查询指定的实例，为空，则查询全部实例
     * @return
     */
    public CanalServerRunningData queryCanalRunningInfo(String destination) {
        CanalServerWithNetty server = embeddedServer.getCanalServerWithNetty();
        CanalServerRunningData data = new CanalServerRunningData(canalHost, hostName, server.getPort());

        try {
            // 获取所有需要查询的CanalInstance对象
            Collection<CanalInstance> instances = null;
            if (StringUtils.isBlank(destination)) {
                instances = embeddedServer.getCanalInstances().values();
            } else {
                instances = new ArrayList<CanalInstance>();
                CanalInstance instance = embeddedServer.getCanalInstances().get(destination);
                if (instance != null) {
                    instances.add(instance);
                }
            }

            // 执行查询
            if (!CollectionUtils.isEmpty(instances)) {
                for (CanalInstance instance : instances) {
                    data.add(instanceRunningData(instance));
                }
            }
        } catch (Exception e) {
            logger.error("query canal running info failed, request: destination=[{}]: cause by:{}\n", destination, ExceptionUtils.getFullStackTrace(e));
            data.setResultCode(CanalServerRunningData.RESULT_CODE_FAILED);
            data.setResultMsg(String.format("query failed: %s", e.getMessage()));
        }

        return data;
    }

    private CanalInstanceRunningData instanceRunningData(CanalInstance instance) {
        CanalInstanceRunningData data = new CanalInstanceRunningData(instance.getDestination());

        try {
            MemoryEventStoreWithBuffer store = (MemoryEventStoreWithBuffer) instance.getEventStore();
            MysqlEventParser parser = (MysqlEventParser) instance.getEventParser();
            data.setMysqlPosition(new LogIdentity(parser.getRunningInfo().getAddress(), parser.getSlaveId()));

            // 获取运行数据
            long bufferSizeTotal = store.getBufferSize();
            long putSequence = store.getPutSequence();
            long ackSequence = store.getAckSequence();

            int bufferMemUnit = store.getBufferMemUnit();
            long putMemCapacity = store.getPutMemSize();
            long ackMemCapacity = store.getAckMemSize();

            // buffer信息(条数)
            data.setBufferSizeTotal(bufferSizeTotal);
            data.setBufferSizeUsed(putSequence - ackSequence);
            data.setBufferSizeRemain(data.getBufferSizeTotal() - data.getBufferSizeUsed());

            // buffer信息(容量)
            data.setBufferCapacityTotal(bufferSizeTotal * bufferMemUnit);
            data.setBufferCapacityUsed(putMemCapacity - ackMemCapacity);
            data.setBufferCapacityRemain(data.getBufferCapacityTotal() - data.getBufferCapacityUsed());

            // position信息
            data.setBinlogPosition(binlogPosition(instance));
            data.setDumpPosition(dumpPosition(instance));
            data.setConsumePositionTable(consumePosition(instance));

            // delay信息
            data.setConsumeDelayTable(consumeDelay(instance));
        } catch (Exception e) {
            logger.error("query canal instance running info failed, request: destination=[{}]: cause by:{}\n", instance.getDestination(), ExceptionUtils.getFullStackTrace(e));
            data.setResultCode(CanalInstanceRunningData.RESULT_CODE_FAILED);
            data.setResultMsg(String.format("query failed: %s", e.getMessage()));
        }

        return data;
    }

    // ================== help method ===================

    /**
     * mysql binlog文件的最新位点
     * 
     * @return
     */
    private EntryPosition binlogPosition(CanalInstance instance) {
        EntryPosition entryPosition = null;
        try {
            MysqlEventParser eventParser = (MysqlEventParser) instance.getEventParser();
            entryPosition = eventParser.findEndPosition(eventParser.getMetaConnection());
        } catch (Exception e) {
            entryPosition = new EntryPosition();
            entryPosition.setJournalName("get failed: " + e.getMessage());
            logger.error("get mysql binlog position failed, destination[{}], cause by: \n{}", instance.getDestination(), ExceptionUtils.getFullStackTrace(e));
        }

        return entryPosition;
    }

    /**
     * canal实例dump的最新位点
     * 
     * @return
     */
    private EntryPosition dumpPosition(CanalInstance instance) {
        EntryPosition entryPosition = null;
        LogPosition logPosition = (LogPosition) instance.getEventStore().getLatestPosition();
        if (logPosition != null) {
            entryPosition = logPosition.getPostion();
        } else {
            entryPosition = new EntryPosition();
            entryPosition.setJournalName("nothing");
        }

        return entryPosition;
    }

    /**
     * canal client消费的最新位点
     * 
     * @return
     */
    private Map<String/* clientId */, EntryPosition> consumePosition(CanalInstance instance) {
        Map<String, EntryPosition> consumePosition = new HashMap<String, EntryPosition>();

        CanalMetaManager metaManager = instance.getMetaManager();
        List<ClientIdentity> clients = metaManager.listAllSubscribeInfo(instance.getDestination());
        if (clients != null) {
            for (ClientIdentity clientIdentity : clients) {
                LogPosition position = (LogPosition) metaManager.getCursor(clientIdentity);
                consumePosition.put(String.valueOf(clientIdentity.getClientId()), position.getPostion());
            }
        }

        return consumePosition;
    }

    /**
     * canal client的消费延迟，暂时以当前时间为依据(因为show master status取不到执行时间)
     * 
     * @return
     */
    private Map<String/* clientId */, Long> consumeDelay(CanalInstance instance) {
        Map<String, Long> consumeDelay = new HashMap<String, Long>();
        long targetTime = System.currentTimeMillis();

        CanalMetaManager metaManager = instance.getMetaManager();
        List<ClientIdentity> clients = metaManager.listAllSubscribeInfo(instance.getDestination());
        if (clients != null) {
            for (ClientIdentity clientIdentity : clients) {
                LogPosition position = (LogPosition) metaManager.getCursor(clientIdentity);
                long delay = targetTime - position.getPostion().getTimestamp();
                consumeDelay.put(String.valueOf(clientIdentity.getClientId()), delay);
            }
        }

        return consumeDelay;
    }

}
