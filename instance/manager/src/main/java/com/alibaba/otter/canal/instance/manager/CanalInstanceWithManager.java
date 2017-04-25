package com.alibaba.otter.canal.instance.manager;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.otter.canal.instance.core.AbstractCanalInstance;
import com.alibaba.otter.canal.meta.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.canal.common.alarm.CanalAlarmHandler;
import com.alibaba.otter.canal.common.alarm.LogAlarmHandler;
import com.alibaba.otter.canal.common.utils.JsonUtils;
import com.alibaba.otter.canal.common.zookeeper.ZkClientx;
import com.alibaba.otter.canal.filter.aviater.AviaterRegexFilter;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.DataSourcing;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.HAMode;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.IndexMode;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.MetaMode;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.SourcingType;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.StorageMode;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.StorageScavengeMode;
import com.alibaba.otter.canal.parse.CanalEventParser;
import com.alibaba.otter.canal.parse.ha.CanalHAController;
import com.alibaba.otter.canal.parse.ha.HeartBeatHAController;
import com.alibaba.otter.canal.parse.inbound.AbstractEventParser;
import com.alibaba.otter.canal.parse.inbound.group.GroupEventParser;
import com.alibaba.otter.canal.parse.inbound.mysql.LocalBinlogEventParser;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.index.CanalLogPositionManager;
import com.alibaba.otter.canal.parse.index.FailbackLogPositionManager;
import com.alibaba.otter.canal.parse.index.MemoryLogPositionManager;
import com.alibaba.otter.canal.parse.index.MetaLogPositionManager;
import com.alibaba.otter.canal.parse.index.PeriodMixedLogPositionManager;
import com.alibaba.otter.canal.parse.index.ZooKeeperLogPositionManager;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.sink.entry.EntryEventSink;
import com.alibaba.otter.canal.sink.entry.group.GroupEventSink;
import com.alibaba.otter.canal.store.AbstractCanalStoreScavenge;
import com.alibaba.otter.canal.store.memory.MemoryEventStoreWithBuffer;
import com.alibaba.otter.canal.store.model.BatchMode;

public class CanalInstanceWithManager extends AbstractCanalInstance {
    private static final Logger logger = LoggerFactory.getLogger(CanalInstanceWithManager.class);

    protected String filter;
    protected CanalParameter parameters;

    public CanalInstanceWithManager(Canal canal, String filter) {
        this.parameters = canal.getCanalParameter();
        this.canalId = canal.getId();
        this.destination = canal.getName();
        this.filter = filter;

        logger.info("init CanalInstance for {}-{} with parameters:{}", canalId, destination, parameters);

        alarmHandler = new LogAlarmHandler();
        metaManager = ManagerMixedMetaManager.getInstance();

        initEventStore();

        eventSink = new EntryEventSink();
        ((EntryEventSink) eventSink).setFilterTransactionEntry(false);
        ((EntryEventSink) eventSink).setEventStore(getEventStore());

        initEventParser();

        // 基础工具，需要提前start，会有先订阅再根据filter条件启动parse的需求
        if (!alarmHandler.isStart()) {
            alarmHandler.start();
        }

        if (!metaManager.isStart()) {
            metaManager.start();
        }

        logger.info("init successful....");
    }

    public void start() {
        logger.info("start CannalInstance for {}-{} with parameters:{}", canalId, destination, parameters);
        super.start();
    }

    protected void initEventStore() {
        MemoryEventStoreWithBuffer memoryEventStore = new MemoryEventStoreWithBuffer();
        memoryEventStore.setBufferSize(parameters.getMemoryStorageBufferSize());
        memoryEventStore.setBufferMemUnit(parameters.getMemoryStorageBufferMemUnit());
        memoryEventStore.setBatchMode(BatchMode.valueOf(parameters.getStorageBatchMode().name()));
        memoryEventStore.setDdlIsolation(parameters.getDdlIsolation());
        eventStore = memoryEventStore;

        if (eventStore instanceof AbstractCanalStoreScavenge) {
            StorageScavengeMode scavengeMode = parameters.getStorageScavengeMode();
            AbstractCanalStoreScavenge eventScavengeStore = (AbstractCanalStoreScavenge) eventStore;
            eventScavengeStore.setDestination(destination);
            eventScavengeStore.setCanalMetaManager(metaManager);
            eventScavengeStore.setOnAck(scavengeMode.isOnAck());
            eventScavengeStore.setOnFull(scavengeMode.isOnFull());
            eventScavengeStore.setOnSchedule(scavengeMode.isOnSchedule());
            if (scavengeMode.isOnSchedule()) {
                eventScavengeStore.setScavengeSchedule(parameters.getScavengeSchdule());
            }
        }
    }

    protected void initEventParser() {
        SourcingType type = parameters.getSourcingType();

        List<List<DataSourcing>> groupDbAddresses = parameters.getGroupDbAddresses();
        if (!CollectionUtils.isEmpty(groupDbAddresses)) {
            int size = groupDbAddresses.get(0).size();// 取第一个分组的数量，主备分组的数量必须一致
            List<CanalEventParser> eventParsers = new ArrayList<CanalEventParser>();
            for (int i = 0; i < size; i++) {
                List<InetSocketAddress> dbAddress = new ArrayList<InetSocketAddress>();
                SourcingType lastType = null;
                for (List<DataSourcing> groupDbAddress : groupDbAddresses) {
                    if (lastType != null && !lastType.equals(groupDbAddress.get(i).getType())) {
                        throw new CanalException(String.format("master/slave Sourcing type is unmatch. %s vs %s", lastType, groupDbAddress.get(i).getType()));
                    }

                    lastType = groupDbAddress.get(i).getType();
                    dbAddress.add(groupDbAddress.get(i).getDbAddress());
                }

                // 初始化其中的一个分组parser
                eventParsers.add(doInitEventParser(lastType, dbAddress));
            }

            if (eventParsers.size() > 1) { // 如果存在分组，构造分组的parser
                GroupEventParser groupEventParser = new GroupEventParser();
                groupEventParser.setEventParsers(eventParsers);
                this.eventParser = groupEventParser;
            } else {
                this.eventParser = eventParsers.get(0);
            }
        } else {
            // 创建一个空数据库地址的parser，可能使用了tddl指定地址，启动的时候才会从tddl获取地址
            this.eventParser = doInitEventParser(type, new ArrayList<InetSocketAddress>());
        }
    }

    private CanalEventParser doInitEventParser(SourcingType type, List<InetSocketAddress> dbAddresses) {
        CanalEventParser eventParser;
        if (type.isMysql()) {
            MysqlEventParser mysqlEventParser = new MysqlEventParser();
            mysqlEventParser.setDestination(destination);

            // 编码参数
            mysqlEventParser.setConnectionCharset(Charset.forName(parameters.getConnectionCharset()));
            mysqlEventParser.setConnectionCharsetNumber(parameters.getConnectionCharsetNumber());

            // 网络相关参数
            mysqlEventParser.setDefaultConnectionTimeoutInSeconds(parameters.getDefaultConnectionTimeoutInSeconds());
            mysqlEventParser.setSendBufferSize(parameters.getSendBufferSize());
            mysqlEventParser.setReceiveBufferSize(parameters.getReceiveBufferSize());

            // 心跳检查参数
            mysqlEventParser.setDetectingEnable(parameters.getDetectingEnable());
            mysqlEventParser.setDetectingSQL(parameters.getDetectingSQL());
            mysqlEventParser.setDetectingIntervalInSeconds(parameters.getDetectingIntervalInSeconds());

            // 数据库信息参数
            mysqlEventParser.setSlaveId(parameters.getSlaveId());
            if (!CollectionUtils.isEmpty(dbAddresses)) {
                mysqlEventParser.setMasterInfo(new AuthenticationInfo(dbAddresses.get(0),
                        parameters.getDbUsername(),
                        parameters.getDbPassword(),
                        parameters.getDefaultDatabaseName()));

                if (dbAddresses.size() > 1) {
                    mysqlEventParser.setStandbyInfo(new AuthenticationInfo(dbAddresses.get(1),
                            parameters.getDbUsername(),
                            parameters.getDbPassword(),
                            parameters.getDefaultDatabaseName()));
                }
            }

            if (!CollectionUtils.isEmpty(parameters.getPositions())) {
                EntryPosition masterPosition = JsonUtils.unmarshalFromString(parameters.getPositions().get(0),
                        EntryPosition.class);
                // binlog位置参数
                mysqlEventParser.setMasterPosition(masterPosition);

                if (parameters.getPositions().size() > 1) {
                    EntryPosition standbyPosition = JsonUtils.unmarshalFromString(parameters.getPositions().get(0),
                            EntryPosition.class);
                    mysqlEventParser.setStandbyPosition(standbyPosition);
                }
            }
            mysqlEventParser.setFallbackIntervalInSeconds(parameters.getFallbackIntervalInSeconds());
            mysqlEventParser.setProfilingEnabled(false);
            mysqlEventParser.setFilterTableError(parameters.getFilterTableError());
            eventParser = mysqlEventParser;
        } else if (type.isLocalBinlog()) {
            LocalBinlogEventParser localBinlogEventParser = new LocalBinlogEventParser();
            localBinlogEventParser.setDestination(destination);
            localBinlogEventParser.setBufferSize(parameters.getReceiveBufferSize());
            localBinlogEventParser.setConnectionCharset(Charset.forName(parameters.getConnectionCharset()));
            localBinlogEventParser.setConnectionCharsetNumber(parameters.getConnectionCharsetNumber());
            localBinlogEventParser.setDirectory(parameters.getLocalBinlogDirectory());
            localBinlogEventParser.setProfilingEnabled(false);
            localBinlogEventParser.setDetectingEnable(parameters.getDetectingEnable());
            localBinlogEventParser.setDetectingIntervalInSeconds(parameters.getDetectingIntervalInSeconds());
            localBinlogEventParser.setFilterTableError(parameters.getFilterTableError());
            // 数据库信息，反查表结构时需要
            if (!CollectionUtils.isEmpty(dbAddresses)) {
                localBinlogEventParser.setMasterInfo(new AuthenticationInfo(dbAddresses.get(0),
                        parameters.getDbUsername(),
                        parameters.getDbPassword(),
                        parameters.getDefaultDatabaseName()));

            }
            eventParser = localBinlogEventParser;
        } else {
            throw new CanalException("unsupport SourcingType for " + type);
        }

        // add transaction support at 2012-12-06
        if (eventParser instanceof AbstractEventParser) {
            AbstractEventParser abstractEventParser = (AbstractEventParser) eventParser;
            abstractEventParser.setTransactionSize(parameters.getTransactionSize());
            abstractEventParser.setLogPositionManager(initLogPositionManager());
            abstractEventParser.setAlarmHandler(getAlarmHandler());
            abstractEventParser.setEventSink(getEventSink());

            if (StringUtils.isNotEmpty(filter)) {
                AviaterRegexFilter aviaterFilter = new AviaterRegexFilter(filter);
                abstractEventParser.setEventFilter(aviaterFilter);
            }

            // 设置黑名单
            if (StringUtils.isNotEmpty(parameters.getBlackFilter())) {
                AviaterRegexFilter aviaterFilter = new AviaterRegexFilter(parameters.getBlackFilter());
                abstractEventParser.setEventBlackFilter(aviaterFilter);
            }
        }
        if (eventParser instanceof MysqlEventParser) {
            MysqlEventParser mysqlEventParser = (MysqlEventParser) eventParser;

            // 初始化haController，绑定与eventParser的关系，haController会控制eventParser
            CanalHAController haController = initHaController();
            mysqlEventParser.setHaController(haController);
        }
        return eventParser;
    }

    protected CanalHAController initHaController() {
        logger.info("init haController begin...");
        HAMode haMode = parameters.getHaMode();
        CanalHAController haController = null;
        if (haMode.isHeartBeat()) {
            haController = new HeartBeatHAController();
            ((HeartBeatHAController) haController).setDetectingRetryTimes(parameters.getDetectingRetryTimes());
            ((HeartBeatHAController) haController).setSwitchEnable(parameters.getHeartbeatHaEnable());
        } else {
            throw new CanalException("unsupport HAMode for " + haMode);
        }
        logger.info("init haController end! load CanalHAController:{}", haController.getClass().getName());

        return haController;
    }

    protected CanalLogPositionManager initLogPositionManager() {
        logger.info("init logPositionPersistManager begin...");
        IndexMode indexMode = parameters.getIndexMode();
        CanalLogPositionManager logPositionManager = null;
        if (indexMode.isMemory()) {
            logPositionManager = new MemoryLogPositionManager();
        } else if (indexMode.isZookeeper()) {
            logPositionManager = new ZooKeeperLogPositionManager();
            ((ZooKeeperLogPositionManager) logPositionManager).setZkClientx(getZkclientx());
        } else if (indexMode.isMixed()) {
            logPositionManager = new PeriodMixedLogPositionManager();

            ZooKeeperLogPositionManager zooKeeperLogPositionManager = new ZooKeeperLogPositionManager();
            zooKeeperLogPositionManager.setZkClientx(getZkclientx());
            ((PeriodMixedLogPositionManager) logPositionManager).setZooKeeperLogPositionManager(zooKeeperLogPositionManager);
        } else if (indexMode.isMeta()) {
            logPositionManager = new MetaLogPositionManager();
            ((MetaLogPositionManager) logPositionManager).setMetaManager(metaManager);
        } else if (indexMode.isMemoryMetaFailback()) {
            MemoryLogPositionManager primaryLogPositionManager = new MemoryLogPositionManager();
            MetaLogPositionManager failbackLogPositionManager = new MetaLogPositionManager();
            failbackLogPositionManager.setMetaManager(metaManager);

            logPositionManager = new FailbackLogPositionManager();
            ((FailbackLogPositionManager) logPositionManager).setPrimary(primaryLogPositionManager);
            ((FailbackLogPositionManager) logPositionManager).setFailback(failbackLogPositionManager);
        } else {
            throw new CanalException("unsupport indexMode for " + indexMode);
        }

        logger.info("init logPositionManager end! load CanalLogPositionManager:{}", logPositionManager.getClass().getName());

        return logPositionManager;
    }

    protected void startEventParserInternal(CanalEventParser eventParser, boolean isGroup) {
        if (eventParser instanceof AbstractEventParser) {
            AbstractEventParser abstractEventParser = (AbstractEventParser) eventParser;
            abstractEventParser.setAlarmHandler(getAlarmHandler());
        }

        super.startEventParserInternal(eventParser, isGroup);
    }

    private synchronized ZkClientx getZkclientx() {
        // 做一下排序，保证相同的机器只使用同一个链接
        List<String> zkClusters = new ArrayList<String>(parameters.getZkClusters());
        Collections.sort(zkClusters);

        return ZkClientx.getZkClient(StringUtils.join(zkClusters, ";"));
    }

}
