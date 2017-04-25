package com.youzan.wagon.persistent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.youzan.wagon.persistent.model.RuleLogicFieldInfo;

public interface RuleLogicFieldInfoDao {
    String ALL_COLMUNS = "rule_id, biz_name, table_name, topic_name, field_name, data_type, field_operator, field_value, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO rule_logic_field_info (" + ALL_COLMUNS + ") VALUES(#{ruleId}, #{bizName}, #{tableName}, #{topicName}, #{fieldName}, #{dataType}, #{fieldOperator}, #{fieldValue}, now(), now())")
    long insert(RuleLogicFieldInfo ruleLogicFieldInfo);

    // ==================== find ====================

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_logic_field_info")
    List<RuleLogicFieldInfo> findAll();

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_logic_field_info WHERE rule_id=#{0}")
    List<RuleLogicFieldInfo> findByRuleId(long ruleId);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_logic_field_info WHERE biz_name=#{0}")
    List<RuleLogicFieldInfo> findByBizName(String bizName);

    // ==================== delete ====================
    @Delete("DELETE FROM rule_logic_field_info WHERE id=#{0}")
    long deleteById(long id);

    @Delete("DELETE FROM rule_logic_field_info WHERE rule_id=#{0}")
    long deleteByRuleId(long ruleId);

}
