package com.youzan.wagon.persistent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.youzan.wagon.persistent.model.RuleOutFieldInfo;

/**
 * @author wangguofeng since 2016年6月3日 下午3:50:18
 */
public interface RuleOutFieldInfoDao {
    String ALL_COLMUNS = "rule_id, biz_name, table_name, topic_name, field_name, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO rule_out_field_info (" + ALL_COLMUNS + ") VALUES (#{ruleId}, #{bizName}, #{tableName}, #{topicName}, #{fieldName}, now(), now())")
    long insert(RuleOutFieldInfo ruleOutFieldInfo);

    // ==================== find ====================
    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_out_field_info")
    List<RuleOutFieldInfo> findAll();

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_out_field_info WHERE id=#{0}")
    RuleOutFieldInfo findById(long id);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_out_field_info WHERE rule_id=#{0}")
    List<RuleOutFieldInfo> findByRuleId(long ruleId);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_out_field_info WHERE biz_name=#{0}")
    List<RuleOutFieldInfo> findByBizName(String bizName);

    // ==================== update ====================
    @Update("UPDATE rule_out_field_info SET biz_name=#{bizName}, table_name=#{tableName}, topic_name=#{topicName}, field_name=#{fieldName}, modify_time=now() WHERE id=#{id}")
    long updateById(RuleOutFieldInfo ruleOutFieldInfo);

    // ==================== delete ====================
    @Delete("DELETE FROM rule_out_field_info WHERE id=#{0}")
    long deleteById(long id);

    @Delete("DELETE FROM rule_out_field_info WHERE rule_id=#{0}")
    long deleteByRuleId(long ruleId);

}
