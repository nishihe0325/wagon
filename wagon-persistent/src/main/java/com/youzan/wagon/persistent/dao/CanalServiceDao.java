package com.youzan.wagon.persistent.dao;

import com.youzan.wagon.persistent.model.CanalInstance;
import com.youzan.wagon.persistent.model.CanalService;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CanalServiceDao {

    String COMMON_COLMUNS = "service_name, rule_biz_name, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO canal_service (" + COMMON_COLMUNS + ") VALUES (#{serviceName}, #{ruleBizName}, now(), now())")
    long insert(CanalService canalService);

    // ==================== find ====================
    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_service")
    List<CanalService> findAll();

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_service WHERE id=#{0}")
    CanalService findById(long id);

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_service WHERE service_name=#{0}")
    CanalService findByServiceName(String serviceName);

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_service WHERE rule_biz_name=#{0}")
    List<CanalService> findByRuleBizName(String ruleBizName);

    // ==================== update ====================
    @Update("UPDATE canal_service SET modify_time=now() WHERE service_name=#{0}")
    long updateModifyTimeByServiceName(String serviceName);

    // ==================== delete ====================
    @Delete("DELETE FROM canal_service WHERE id=#{0}")
    long deleteById(long id);

}
