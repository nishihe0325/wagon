package com.youzan.wagon.persistent.dao;

import com.youzan.wagon.persistent.model.CanalServer;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.model.RuleBizInfo;
import com.youzan.wagon.persistent.model.RuleInfo;
import com.youzan.wagon.persistent.provide.CanalServerInfoProvide;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface CanalServerDao {

    String COMMON_COLMUNS = "service_name, role, service_state, host, host_name, port, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO canal_server (" + COMMON_COLMUNS + ") VALUES (#{serviceName}, #{role}, #{serviceState}, #{host}, #{hostName}, #{port}, now(), now())")
    long insert(CanalServer canalServer);

    // ==================== find ====================
    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_server")
    List<CanalServer> findAll();

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_server WHERE id=#{0}")
    CanalServer findById(long id);

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_server WHERE service_name=#{0}")
    List<CanalServer> findByServiceName(String serviceName);

    // ==================== update ====================
    @Update("UPDATE canal_server SET modify_time=now() WHERE id=#{1}")
    long updateModifyTime(long id);

    // ==================== delete ====================
    @Delete("DELETE FROM canal_server WHERE id=#{0}")
    long deleteById(long id);

}
