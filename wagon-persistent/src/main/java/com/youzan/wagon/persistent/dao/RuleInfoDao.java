package com.youzan.wagon.persistent.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.youzan.wagon.persistent.model.RuleInfo;
import com.youzan.wagon.persistent.provide.RuleInfoSqlProvider;

/**
 * @author wangguofeng since 2016年5月24日 上午10:36:29
 */
public interface RuleInfoDao {
    String ALL_COLMUNS = "biz_name, table_name, schema_name, topic_name, event_type, enable_state, push_data_type, field_condition_type, out_type, rule_owner, organization, seq_consume_field, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO rule_info (" + ALL_COLMUNS + ") VALUES (#{bizName}, #{tableName}, #{schemaName}, #{topicName}, #{eventType}, #{enableState}, #{pushDataType}, #{fieldConditionType}, #{outType}, #{ruleOwner}, #{organization}, #{seqConsumeField}, now(), now())")
    long insert(RuleInfo ruleInfo);

    // ==================== find ====================
    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_info")
    List<RuleInfo> findAll();

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_info WHERE id=#{0}")
    RuleInfo findById(long id);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_info WHERE biz_name=#{0}")
    List<RuleInfo> findByBizName(String bizName);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_info WHERE table_name=#{0}")
    List<RuleInfo> findByTableName(String tableName);

    @SelectProvider(type = RuleInfoSqlProvider.class, method = "findByMapSql")
    List<RuleInfo> findByMap(Map<String, Object> condition);

    // ==================== update ====================
    @Update("UPDATE rule_info SET biz_name=#{bizName}, table_name=#{tableName}, schema_name=#{schemaName}, topic_name=#{topicName}, event_type=#{eventType}, enable_state=#{enableState}, push_data_type=#{pushDataType}, field_condition_type=#{fieldConditionType}, out_type=#{outType}, rule_owner=#{ruleOwner}, organization=#{organization}, seq_consume_field=#{seqConsumeField}, modify_time=now() WHERE id=#{id}")
    long updateById(RuleInfo ruleInfo);

    @Update("UPDATE rule_info SET enable_state=#{0}, modify_time=now() WHERE id=#{1}")
    long updateEnableState(short enableState, long id);

    // ==================== delete ====================
    @Delete("DELETE FROM rule_info WHERE id=#{0}")
    long deleteById(long id);

}
