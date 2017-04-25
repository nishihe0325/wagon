package com.youzan.wagon.persistent.service;

import com.youzan.wagon.persistent.model.CanalInstance;

import java.util.List;

public interface CanalInstanceService {

    // ==================== insert ====================
    long insert(CanalInstance canalInstance);

    // ==================== find ====================
    List<CanalInstance> findAll();

    CanalInstance findById(long id);

    List<CanalInstance> findByServiceId(long serviceId);

    List<CanalInstance> findByServiceName(String serviceName);

    CanalInstance findByServiceNameAndInstanceName(String serviceName, String instanceName);

    // ==================== update ====================
    boolean updateByServiceNameAndInstanceName(CanalInstance canalInstance);

    boolean emptyBinlogPositionByServiceName(String serviceName);

    // ==================== delete ====================
    long deleteById(long id);

}
