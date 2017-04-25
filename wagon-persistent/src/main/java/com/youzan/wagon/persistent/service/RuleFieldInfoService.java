package com.youzan.wagon.persistent.service;

import java.util.List;

import com.youzan.wagon.persistent.model.RuleFieldInfo;

/**
 * @author wangguofeng since 2016年5月24日 上午10:49:03
 */
public interface RuleFieldInfoService {

    // ==================== insert ====================
    long insert(RuleFieldInfo ruleFieldInfo);

    // ==================== find ====================

    RuleFieldInfo findById(long id);

    List<RuleFieldInfo> findAll();

    List<RuleFieldInfo> findByRuleId(long ruleId);

    List<RuleFieldInfo> findByBizName(String bizName);

    // ==================== update ====================
    long updateById(RuleFieldInfo ruleFieldInfo);

    // ==================== delete ====================
    long deleteById(long id);

    long deleteByRuleId(long ruleId);

}
