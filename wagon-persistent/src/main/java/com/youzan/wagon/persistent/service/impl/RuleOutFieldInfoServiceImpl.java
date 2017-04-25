package com.youzan.wagon.persistent.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.RuleOutFieldInfoDao;
import com.youzan.wagon.persistent.model.RuleOutFieldInfo;
import com.youzan.wagon.persistent.service.RuleOutFieldInfoService;

/**
 * @author wangguofeng since 2016年5月24日 上午10:48:16
 */
@Component("ruleOutFieldInfoService")
public class RuleOutFieldInfoServiceImpl implements RuleOutFieldInfoService {

    @Autowired
    private RuleOutFieldInfoDao dao;

    // ==================== insert ====================
    @Override
    public long insert(RuleOutFieldInfo ruleOutFieldInfo) {
        return dao.insert(ruleOutFieldInfo);
    }

    // ==================== find ====================
    @Override
    public RuleOutFieldInfo findById(long id) {
        return dao.findById(id);
    }

    @Override
    public List<RuleOutFieldInfo> findAll() {
        return dao.findAll();
    }

    @Override
    public List<RuleOutFieldInfo> findByRuleId(long ruleId) {
        return dao.findByRuleId(ruleId);
    }

    @Override
    public List<RuleOutFieldInfo> findByBizName(String bizName) {
        return dao.findByBizName(bizName);
    }

    // ==================== update ====================
    @Override
    public long updateById(RuleOutFieldInfo ruleOutFieldInfo) {
        return dao.updateById(ruleOutFieldInfo);
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
