package com.youzan.wagon.persistent.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.RuleFieldInfoDao;
import com.youzan.wagon.persistent.dao.RuleInfoDao;
import com.youzan.wagon.persistent.dao.RuleOutFieldInfoDao;
import com.youzan.wagon.persistent.model.RuleInfo;
import com.youzan.wagon.persistent.service.RuleInfoService;

/**
 * @author wangguofeng since 2016年5月24日 上午10:48:33
 */
@Component("ruleInfoService")
public class RuleInfoServiceImpl implements RuleInfoService {

    @Autowired
    private RuleInfoDao dao;
    @Autowired
    private RuleFieldInfoDao fieldInfoDao;
    @Autowired
    private RuleOutFieldInfoDao outFieldInfoDao;

    // ==================== insert ====================
    public long insert(RuleInfo ruleInfo) {
        return dao.insert(ruleInfo);
    }

    // ==================== find ====================
    @Override
    public List<RuleInfo> findAll() {
        return dao.findAll();
    }

    @Override
    public RuleInfo findById(long id) {
        return dao.findById(id);
    }

    @Override
    public List<RuleInfo> findByBizName(String bizName) {
        return dao.findByBizName(bizName);
    }

    @Override
    public List<RuleInfo> findByTableName(String tableName) {
        return dao.findByTableName(tableName);
    }

    @Override
    public List<RuleInfo> findByMap(Map<String, Object> condition) {
        return dao.findByMap(condition);
    }

    // ==================== update ====================
    @Override
    public long updateById(RuleInfo ruleInfo) {
        return dao.updateById(ruleInfo);
    }

    public long updateEnableState(short enableState, long id) {
        return dao.updateEnableState(enableState, id);
    }

    // ==================== delete ====================
    @Override
    public long deleteById(long id) {
        // 需要事务，todo
        long count = dao.deleteById(id);
        fieldInfoDao.deleteByRuleId(id);
        outFieldInfoDao.deleteByRuleId(id);
        return count;
    }

}
