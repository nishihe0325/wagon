package com.youzan.wagon.persistent.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.RuleBizInfoDao;
import com.youzan.wagon.persistent.model.RuleBizInfo;
import com.youzan.wagon.persistent.service.RuleBizInfoService;

@Component("ruleBizInfoService")
public class RuleBizInfoServiceImpl implements RuleBizInfoService {

    @Autowired
    private RuleBizInfoDao dao;

    // ==================== insert ====================
    public long insert(RuleBizInfo ruleBizInfo) {
        return dao.insert(ruleBizInfo);
    }

    // ==================== find ====================
    public List<String> findAllNames() {
        return dao.findAllNames();
    }

    public List<RuleBizInfo> findAll() {
        return dao.findAll();
    }

    public RuleBizInfo findById(long id) {
        return dao.findById(id);
    }

    public RuleBizInfo findByBizName(String bizName) {
        return dao.findByBizName(bizName);
    }

    // ==================== update ====================
    public long updateVersionById(long id, String version) {
        return dao.updateVersionById(id, version);
    }

    // ==================== delete ====================
    public long deleteById(long id) {
        return dao.deleteById(id);
    }

}
