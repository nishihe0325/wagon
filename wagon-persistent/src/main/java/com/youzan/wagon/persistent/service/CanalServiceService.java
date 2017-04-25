package com.youzan.wagon.persistent.service;

import com.youzan.wagon.persistent.model.CanalService;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CanalServiceService {

    // ==================== insert ====================
    long insert(CanalService canalService);

    // ==================== find ====================
    List<CanalService> findAll();

    CanalService findById(long id);

    CanalService findByServiceName(String serviceName);

    List<CanalService> findByRuleBizName(String ruleBizName);

    // ==================== update ====================
    long updateModifyTimeByServiceName(String serviceName);

    // ==================== delete ====================
    long deleteById(long id);

}
