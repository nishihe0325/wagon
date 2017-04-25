package com.youzan.wagon.persistent.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.RuleLogicFieldInfoDao;
import com.youzan.wagon.persistent.model.RuleLogicFieldInfo;
import com.youzan.wagon.persistent.service.RuleLogicFieldInfoService;

/**
 * @author wangguofeng since 2016年5月24日 上午10:48:59
 */
@Component("LogicFieldInfoService")
public class RuleLogicFieldInfoServiceImpl implements RuleLogicFieldInfoService {

    @Autowired
    private RuleLogicFieldInfoDao dao;

    // ==================== insert ====================
    @Override
    public long insert(RuleLogicFieldInfo ruleLogicFieldInfo) {
        return dao.insert(ruleLogicFieldInfo);
    }

    // ==================== find ====================
    @Override
    public List<RuleLogicFieldInfo> findByRuleId(long ruleId) {
        return dao.findByRuleId(ruleId);
    }

    @Override
    public List<RuleLogicFieldInfo> findAll() {
        return dao.findAll();
    }

    @Override
    public List<RuleLogicFieldInfo> findByBizName(String bizName) {
        return dao.findByBizName(bizName);
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
