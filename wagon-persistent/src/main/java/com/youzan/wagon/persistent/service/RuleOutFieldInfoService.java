package com.youzan.wagon.persistent.service;

import java.util.List;

import com.youzan.wagon.persistent.model.RuleOutFieldInfo;

/**
 * @author wangguofeng since 2016年5月24日 下午12:07:08
 */
public interface RuleOutFieldInfoService {

    // ==================== insert ====================
    long insert(RuleOutFieldInfo ruleOutFieldInfo);

    // ==================== find ====================
    RuleOutFieldInfo findById(long id);

    List<RuleOutFieldInfo> findAll();

    List<RuleOutFieldInfo> findByRuleId(long ruleId);

    List<RuleOutFieldInfo> findByBizName(String bizName);

    // ==================== update ====================
    long updateById(RuleOutFieldInfo ruleOutFieldInfo);

    // ==================== delete ====================
    long deleteById(long id);

    long deleteByRuleId(long ruleId);
}
