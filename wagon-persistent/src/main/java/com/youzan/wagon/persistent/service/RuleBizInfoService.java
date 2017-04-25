package com.youzan.wagon.persistent.service;

import java.util.List;

import com.youzan.wagon.persistent.model.RuleBizInfo;

public interface RuleBizInfoService {

    // ==================== insert ====================
    long insert(RuleBizInfo ruleBizInfo);

    // ==================== find ====================
    List<String> findAllNames();

    List<RuleBizInfo> findAll();

    RuleBizInfo findById(long id);

    RuleBizInfo findByBizName(String bizName);

    // ==================== update ====================
    long updateVersionById(long id, String version);

    // ==================== delete ====================
    long deleteById(long id);

}
