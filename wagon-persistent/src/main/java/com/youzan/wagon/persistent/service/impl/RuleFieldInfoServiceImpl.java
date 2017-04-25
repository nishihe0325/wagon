package com.youzan.wagon.persistent.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.RuleFieldInfoDao;
import com.youzan.wagon.persistent.model.RuleFieldInfo;
import com.youzan.wagon.persistent.service.RuleFieldInfoService;

/**
 * @author wangguofeng since 2016年5月24日 上午10:48:59
 */
@Component("ruleFieldInfoService")
public class RuleFieldInfoServiceImpl implements RuleFieldInfoService {

    @Autowired
    private RuleFieldInfoDao dao;

    // ==================== insert ====================
    @Override
    public long insert(RuleFieldInfo ruleFieldInfo) {
        return dao.insert(ruleFieldInfo);
    }

    // ==================== find ====================
    @Override
    public RuleFieldInfo findById(long id) {
        return dao.findById(id);
    }

    @Override
    public List<RuleFieldInfo> findAll() {
        return dao.findAll();
    }

    @Override
    public List<RuleFieldInfo> findByRuleId(long ruleId) {
        return dao.findByRuleId(ruleId);
    }

    @Override
    public List<RuleFieldInfo> findByBizName(String bizName) {
        return dao.findByBizName(bizName);
    }

    // ==================== update ====================
    @Override
    public long updateById(RuleFieldInfo ruleFieldInfo) {
        return dao.updateById(ruleFieldInfo);
    }

    // ==================== delete ====================
    @Override
    public long deleteById(long id) {
        return dao.deleteById(id);
    }

    @Override
    public long deleteByRuleId(long ruleId) {
        return dao.deleteByRuleId(ruleId);
    }

}
