package com.alibaba.otter.canal.deployer.monitor;

import com.alibaba.otter.canal.common.AbstractCanalLifeCycle;
import com.alibaba.otter.canal.common.CanalLifeCycle;

public abstract class AbstractInstanceConfigMonitor extends AbstractCanalLifeCycle implements InstanceConfigMonitor, CanalLifeCycle {

    @Override
    public void register(String destination, InstanceAction action) {
    }

    @Override
    public void unregister(String destination) {
    }

}
