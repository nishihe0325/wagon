package com.youzan.wagon.persistent.service;

import java.util.List;
import java.util.Map;

import com.youzan.wagon.persistent.model.CanalInstanceInfo;

public interface CanalInstanceInfoService {

    // ==================== insert ====================

    void insert(CanalInstanceInfo info);

    // ==================== find ====================

    List<CanalInstanceInfo> findAll();

    CanalInstanceInfo findById(Long id);

    List<CanalInstanceInfo> findByHostAndPort(String canalHost, Integer canalPort);

    List<CanalInstanceInfo> findByHostAndPortAndDest(String canalHost, Integer canalPort, String destination);

    List<CanalInstanceInfo> findByMap(Map<String, Object> condition);

    // ==================== update ====================

    boolean updateDBInfo(CanalInstanceInfo info);

    // ==================== delete ====================
    void deleteById(Long id);

}
