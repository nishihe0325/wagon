package com.alibaba.otter.canal.instance.manager;

import com.alibaba.otter.canal.common.protocol.CanalInstanceDataV2;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ManagerCanalInstanceGenerator implements CanalInstanceGenerator {

    private CanalInstanceContainer canalInstanceContainer = CanalInstanceContainer.getInstance();

    // ================ singleton ==============================================
    private ManagerCanalInstanceGenerator() {
    }

    private static ManagerCanalInstanceGenerator singleton;

    public static ManagerCanalInstanceGenerator getInstance() {
        if (singleton == null) {
            synchronized (ManagerCanalInstanceGenerator.class) {
                if (singleton == null) {
                    singleton = new ManagerCanalInstanceGenerator();
                }
            }
        }

        return singleton;
    }

    // ================ operator ================
    public CanalInstance generate(String destination) {
        CanalInstanceDataV2 instanceData = canalInstanceContainer.getInstance(destination);
        List<InetSocketAddress> dbAddresses = new ArrayList<InetSocketAddress>();
        dbAddresses.add(new InetSocketAddress(instanceData.getDbHost(), instanceData.getDbPort()));

        CanalParameter canalParameter = new CanalParameter();
        canalParameter.setSlaveId(instanceData.getSlaveId());
        canalParameter.setDbAddresses(dbAddresses);
        canalParameter.setDbUsername(instanceData.getDbUsername());
        canalParameter.setDbPassword(instanceData.getDbPassword()); // 需要改为加密
        canalParameter.setMasterLogfileName(instanceData.getBinlogFile());
        canalParameter.setMasterLogfileOffest(instanceData.getBinlogOffset());
        canalParameter.setMasterTimestamp(instanceData.getBinlogExeTime());

        Canal canal = new Canal();
        canal.setName(destination);
        canal.setCanalParameter(canalParameter);

        return new CanalInstanceWithManager(canal, null);
    }

}
