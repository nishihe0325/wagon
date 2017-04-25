package com.youzan.wagon.persistent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.youzan.wagon.persistent.model.RuleFieldInfo;

/**
 * @author wangguofeng since 2016年5月24日 上午10:36:29
 */
public interface RuleFieldInfoDao {
    String ALL_COLMUNS = "rule_id, biz_name, table_name, topic_name, position_type, field_name, field_operator, field_value, must_updated, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO rule_field_info (" + ALL_COLMUNS + ") VALUES(#{ruleId}, #{bizName}, #{tableName}, #{topicName}, #{positionType}, #{fieldName}, #{fieldOperator}, #{fieldValue}, #{mustUpdated}, now(), now())")
    long insert(RuleFieldInfo ruleFieldInfo);

    // ==================== find ====================
    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_field_info")
    List<RuleFieldInfo> findAll();

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_field_info WHERE id=#{0}")
    RuleFieldInfo findById(long id);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_field_info WHERE rule_id=#{0}")
    List<RuleFieldInfo> findByRuleId(long ruleId);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_field_info WHERE biz_name=#{0}")
    List<RuleFieldInfo> findByBizName(String bizName);

    // ==================== update ====================
    @Update("UPDATE rule_field_info SET biz_name=#{bizName}, table_name=#{tableName}, topic_name=#{topicName}, position_type=#{positionType}, field_name=#{fieldName}, field_operator=#{fieldOperator}, field_value=#{fieldValue}, must_updated=#{mustUpdated}, modify_time=now() WHERE id=#{id}")
    long updateById(RuleFieldInfo ruleFieldInfo);

    // ==================== delete ====================
    @Delete("DELETE FROM rule_field_info WHERE id=#{0}")
    long deleteById(long id);

    @Delete("DELETE FROM rule_field_info WHERE rule_id=#{0}")
    long deleteByRuleId(long ruleId);

}
