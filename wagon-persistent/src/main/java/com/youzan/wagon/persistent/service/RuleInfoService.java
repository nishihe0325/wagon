package com.youzan.wagon.persistent.service;

import java.util.List;
import java.util.Map;

import com.youzan.wagon.persistent.model.RuleInfo;

/**
 * @author wangguofeng since 2016年5月24日 上午10:48:38
 */
public interface RuleInfoService {

    // ==================== insert ====================
    long insert(RuleInfo ruleInfo);

    // ==================== find ====================
    List<RuleInfo> findAll();

    RuleInfo findById(long id);

    List<RuleInfo> findByBizName(String bizName);

    List<RuleInfo> findByTableName(String tableName);

    List<RuleInfo> findByMap(Map<String, Object> condition);

    // ==================== update ====================
    long updateById(RuleInfo ruleInfo);

    long updateEnableState(short enableState, long id);

    // ==================== delete ====================
    long deleteById(long id);

}
