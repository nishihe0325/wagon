package com.alibaba.otter.canal.instance.manager;

import com.alibaba.otter.canal.common.protocol.CanalInstanceDataV2;

import java.util.*;

public class CanalInstanceContainer {

    // ================================ singleton ========================================
    private static CanalInstanceContainer singleton;

    private CanalInstanceContainer() {
    }

    public static CanalInstanceContainer getInstance() {
        if (singleton == null) {
            synchronized (CanalInstanceContainer.class) {
                if (singleton == null) {
                    singleton = new CanalInstanceContainer();
                }
            }
        }

        return singleton;
    }

    // ===================================================================================
    private String version;
    private final Map<String, CanalInstanceDataV2> instances = new HashMap<String, CanalInstanceDataV2>();

    // ======================= operator ==================================================
    public CanalInstanceDataV2 addInstance(CanalInstanceDataV2 canalInstanceData) {
        return instances.put(canalInstanceData.getInstanceName(), canalInstanceData);
    }

    public CanalInstanceDataV2 removeInstance(String instanceName) {
        return instances.remove(instanceName);
    }

    public CanalInstanceDataV2 getInstance(String instanceName) {
        return instances.get(instanceName);
    }

    public int size() {
        return instances != null ? instances.size() : 0;
    }

    // ========================get and set========================
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, CanalInstanceDataV2> getInstances() {
        return instances;
    }

}
