package com.youzan.wagon.persistent.service;

import java.util.List;

import com.youzan.wagon.persistent.model.RuleLogicFieldInfo;

/**
 * @author wangguofeng since 2016年5月24日 上午10:49:03
 */
public interface RuleLogicFieldInfoService {

    // ==================== insert ====================
    long insert(RuleLogicFieldInfo ruleLogicFieldInfo);

    // ==================== find ====================
    List<RuleLogicFieldInfo> findByRuleId(long ruleId);

    List<RuleLogicFieldInfo> findAll();

    List<RuleLogicFieldInfo> findByBizName(String bizName);

    // ==================== delete ====================
    long deleteById(long id);

    long deleteByRuleId(long ruleId);

}
