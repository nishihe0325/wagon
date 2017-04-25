package com.alibaba.otter.canal.meta;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.otter.canal.common.utils.JsonUtils;
import com.alibaba.otter.canal.meta.exception.CanalMetaManagerException;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.protocol.position.Position;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MigrateMap;
import org.springframework.util.CollectionUtils;

/**
 * 基于文件刷新和同步到管理器的metaManager实现
 * <pre>
 * 策略：
 * 1. 先写内存
 * 2. 定时刷新数据到File和同步到远程管理器
 * 3. 数据采取overwrite模式,只保留最后一次
 * 4. 通过logger实施append模式(记录历史版本)
 * </pre>
 */
public class ManagerMixedMetaManager extends MemoryMetaManager implements CanalMetaManager {
    private static final Logger logger = LoggerFactory.getLogger(ManagerMixedMetaManager.class);

    private static final Charset charset = Charset.forName("UTF-8");
    private static final String DefaultDataDir = "../conf";
    private static final String DefaultDataFileName = "meta.dat";

    private File dataDir;
    private String dataFileName = DefaultDataFileName;
    private Map<String, File> dataFileCaches;

    @SuppressWarnings("serial")
    private final Position nullCursor = new Position() {
    };
    private Set<ClientIdentity> updateCursorTasks;

    private long period = 1000;
    private ScheduledExecutorService executor;

    // ======================== Single ===========================================
    private static ManagerMixedMetaManager instance;

    public static ManagerMixedMetaManager getInstance() {
        if (instance == null) {
            synchronized (ManagerMixedMetaManager.class) {
                if (instance == null) {
                    instance = new ManagerMixedMetaManager();
                }
            }
        }
        return instance;
    }

    private ManagerMixedMetaManager() {
        // 新建数据文件目录对象,如果不存在,则建立,并确保可读写
        dataDir = new File(DefaultDataDir);
        if (!dataDir.exists()) {
            try {
                FileUtils.forceMkdir(dataDir);
            } catch (IOException e) {
                throw new CanalMetaManagerException(e);
            }
        }
        if (!dataDir.canRead() || !dataDir.canWrite()) {
            throw new CanalMetaManagerException("dir[" + dataDir.getPath() + "] can not read/write");
        }

        updateCursorTasks = Collections.synchronizedSet(new HashSet<ClientIdentity>());

        dataFileCaches = MigrateMap.makeComputingMap(new Function<String, File>() {
            public File apply(String destination) {
                File destinationMetaDir = new File(dataDir, destination);
                if (!destinationMetaDir.exists()) {
                    try {
                        FileUtils.forceMkdir(destinationMetaDir);
                    } catch (IOException e) {
                        throw new CanalMetaManagerException(e);
                    }
                }
                return new File(destinationMetaDir, dataFileName);
            }
        });
        executor = Executors.newScheduledThreadPool(1);
    }

    // =========================================================================
    public void start() {
        super.start();

        destinations = MigrateMap.makeComputingMap(new Function<String, List<ClientIdentity>>() {
            public List<ClientIdentity> apply(String destination) {
                List<ClientIdentity> result = Lists.newArrayList();

                FileMetaInstanceData data = loadFileMetaInstanceData(dataFileCaches.get(destination));
                if (data != null && !CollectionUtils.isEmpty(data.getClientDatas())) {
                    for (FileMetaClientIdentityData clientData : data.getClientDatas()) {
                        if (clientData.getClientIdentity().getDestination().equals(destination)) {
                            result.add(clientData.getClientIdentity());
                        }
                    }
                }

                return result;
            }
        });

        cursors = MigrateMap.makeComputingMap(new Function<ClientIdentity, Position>() {
            public Position apply(ClientIdentity clientIdentity) {
                Position position = null;

                FileMetaInstanceData data = loadFileMetaInstanceData(dataFileCaches.get(clientIdentity.getDestination()));
                if (data != null && !CollectionUtils.isEmpty(data.getClientDatas())) {
                    for (FileMetaClientIdentityData clientData : data.getClientDatas()) {
                        if (clientData.getClientIdentity() != null && clientData.getClientIdentity().equals(clientIdentity)) {
                            position = clientData.getCursor();
                            continue;
                        }
                    }
                }

                return position != null ? position : nullCursor; // 为null,则返回一个空对象标识，避免出现异常
            }
        });


        // 启动定时任务
        executor.scheduleAtFixedRate(
                () -> {
                    List<ClientIdentity> tasks = new ArrayList<ClientIdentity>(updateCursorTasks);
                    for (ClientIdentity clientIdentity : tasks) {
                        MDC.put("destination", String.valueOf(clientIdentity.getDestination()));
                        try {
                            LogPosition cursor = (LogPosition) getCursor(clientIdentity);
                            logger.info("clientId:{} cursor:[{},{},{}] address[{}]", new Object[]{clientIdentity.getClientId(), cursor.getPostion().getJournalName(), cursor.getPostion().getPosition(), cursor.getPostion().getTimestamp(), cursor.getIdentity().getSourceAddress().toString()});
                            flushData(clientIdentity.getDestination());
                            updateCursorTasks.remove(clientIdentity);
                        } catch (Throwable e) {
                            logger.error("period update" + clientIdentity.toString() + " curosr failed!", e);
                        }
                    }
                }, period, period, TimeUnit.MILLISECONDS
        );
    }

    public void stop() {
        super.stop();

        // 刷新数据
        for (String destination : destinations.keySet()) {
            flushData(destination);
        }

        executor.shutdownNow();
        destinations.clear();
        batches.clear();
    }

    public void subscribe(final ClientIdentity clientIdentity) throws CanalMetaManagerException {
        super.subscribe(clientIdentity);

        // 订阅信息频率发生比较低，不需要做定时merge处理
        executor.submit(new Runnable() {
            public void run() {
                flushData(clientIdentity.getDestination());
            }
        });
    }

    public void unsubscribe(final ClientIdentity clientIdentity) throws CanalMetaManagerException {
        super.unsubscribe(clientIdentity);

        // 订阅信息频率发生比较低，不需要做定时merge处理
        executor.submit(new Runnable() {
            public void run() {
                flushData(clientIdentity.getDestination());
            }
        });
    }

    public void updateCursor(ClientIdentity clientIdentity, Position position) throws CanalMetaManagerException {
        updateCursorTasks.add(clientIdentity);// 添加到任务队列中进行触发
        super.updateCursor(clientIdentity, position);
    }

    public Position getCursor(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        Position position = super.getCursor(clientIdentity);
        if (position == nullCursor) {
            return null;
        } else {
            return position;
        }
    }

    // ============================ helper method ======================
    private FileMetaInstanceData loadFileMetaInstanceData(File dataFile) {
        try {
            if (!dataFile.exists()) {
                return null;
            }

            String json = FileUtils.readFileToString(dataFile, charset.name());
            return JsonUtils.unmarshalFromString(json, FileMetaInstanceData.class);
        } catch (IOException e) {
            throw new CanalMetaManagerException(e);
        }
    }

    private void flushData(String destination) {
        if (destinations.containsKey(destination)) {
            FileMetaInstanceData data = new FileMetaInstanceData();
            synchronized (destination.intern()) { // 基于destination控制一下并发更新
                data.setDestination(destination);

                List<FileMetaClientIdentityData> clientDatas = Lists.newArrayList();
                List<ClientIdentity> clientIdentitys = destinations.get(destination);
                for (ClientIdentity clientIdentity : clientIdentitys) {
                    FileMetaClientIdentityData clientData = new FileMetaClientIdentityData();
                    clientData.setClientIdentity(clientIdentity);
                    Position position = cursors.get(clientIdentity);
                    if (position != null && position != nullCursor) {
                        clientData.setCursor((LogPosition) position);
                    }

                    clientDatas.add(clientData);
                }

                data.setClientDatas(clientDatas);
            }

            String json = JsonUtils.marshalToString(data);

            // 刷新到文件
            try {
                FileUtils.writeStringToFile(dataFileCaches.get(destination), json);
            } catch (IOException e) {
                throw new CanalMetaManagerException(e);
            }
        }
    }

    /**
     * 描述一个clientIdentity对应的数据对象
     *
     * @author jianghang 2013-4-15 下午06:19:40
     * @version 1.0.4
     */
    public static class FileMetaClientIdentityData {
        private ClientIdentity clientIdentity;
        private LogPosition cursor;

        public ClientIdentity getClientIdentity() {
            return clientIdentity;
        }

        public void setClientIdentity(ClientIdentity clientIdentity) {
            this.clientIdentity = clientIdentity;
        }

        public Position getCursor() {
            return cursor;
        }

        public void setCursor(LogPosition cursor) {
            this.cursor = cursor;
        }
    }

    /**
     * 描述整个canal instance对应数据对象
     *
     * @author jianghang 2013-4-15 下午06:20:22
     * @version 1.0.4
     */
    public static class FileMetaInstanceData {
        private String destination;
        private List<FileMetaClientIdentityData> clientDatas;

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public List<FileMetaClientIdentityData> getClientDatas() {
            return clientDatas;
        }

        public void setClientDatas(List<FileMetaClientIdentityData> clientDatas) {
            this.clientDatas = clientDatas;
        }
    }

}
