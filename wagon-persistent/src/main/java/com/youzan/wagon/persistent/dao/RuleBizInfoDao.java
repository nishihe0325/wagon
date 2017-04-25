package com.youzan.wagon.persistent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.youzan.wagon.persistent.model.RuleBizInfo;

public interface RuleBizInfoDao {
    String ALL_COLMUNS = "biz_name, version, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO rule_biz_info (" + ALL_COLMUNS + ") VALUES (#{bizName}, #{version}, now(), now())")
    long insert(RuleBizInfo ruleBizInfo);

    // ==================== find ====================
    @Select("SELECT biz_name FROM rule_biz_info")
    List<String> findAllNames();

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_biz_info")
    List<RuleBizInfo> findAll();

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_biz_info WHERE id=#{0}")
    RuleBizInfo findById(long id);

    @Select("SELECT id, " + ALL_COLMUNS + " FROM rule_biz_info WHERE biz_name=#{0}")
    RuleBizInfo findByBizName(String bizName);

    // ==================== update ====================
    @Update("UPDATE rule_biz_info SET version=#{1}, modify_time=now() WHERE id=#{0}")
    long updateVersionById(long id, String version);

    // ==================== delete ====================
    @Delete("DELETE FROM rule_biz_info WHERE id=#{0}")
    long deleteById(long id);

}
