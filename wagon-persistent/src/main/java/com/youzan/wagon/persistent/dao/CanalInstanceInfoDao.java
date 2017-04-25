package com.youzan.wagon.persistent.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.youzan.wagon.persistent.model.CanalInstanceInfo;
import com.youzan.wagon.persistent.provide.CanalInstanceInfoProvide;

/**
 * @author wangguofeng since 2016年1月7日 下午11:49:06
 */
public interface CanalInstanceInfoDao {

    // ==================== insert ====================

    @Insert("INSERT INTO canal_instance_info "//
            + "(canal_host_name, canal_host, canal_port, destination, slave_id, db_host, db_port, db_username, create_time, modify_time) " //
            + "VALUES(#{canalHostName}, #{canalHost}, #{canalPort}, #{destination}, #{slaveId}, #{dbHost}, #{dbPort}, #{dbUsername}, now(), now())")
    void insert(CanalInstanceInfo info);

    // ==================== find ====================

    @Select("SELECT * FROM canal_instance_info")
    List<CanalInstanceInfo> findAll();

    @Select("SELECT * FROM canal_instance_info " //
            + "WHERE id=#{0}")
    CanalInstanceInfo findById(Long id);

    @Select("SELECT * FROM canal_instance_info " //
            + "WHERE canal_host=#{0}")
    List<CanalInstanceInfo> findByHost(String canalHost);

    @Select("SELECT * FROM canal_instance_info " //
            + "WHERE canal_host=#{0} AND canal_port=#{1}")
    List<CanalInstanceInfo> findByHostAndPort(String canalHost, Integer canalPort);

    @Select("SELECT * FROM canal_instance_info " //
            + "WHERE canal_host=#{0} AND canal_port=#{1} AND destination=#{2}")
    List<CanalInstanceInfo> findByHostAndPortAndDest(String canalHost, Integer canalPort, String destination);

    @SelectProvider(type = CanalInstanceInfoProvide.class, method = "getByMap")
    List<CanalInstanceInfo> findByMap(Map<String, Object> condition);

    // ==================== update ====================

    @Update("UPDATE canal_instance_info " //
            + "SET slave_id = #{slaveId}, db_host = #{dbHost}, db_port = #{dbPort}, db_username = #{dbUsername}, modify_time = now() "//
            + "WHERE canal_host=#{canalHost} AND canal_port=#{canalPort} AND destination=#{destination}")
    @Options(useGeneratedKeys = true)
    boolean updateDBInfo(CanalInstanceInfo info);

    // ==================== delete ====================
    @Select("DELETE FROM canal_instance_info " //
            + "WHERE id=#{0}")
    void deleteById(Long id);

}
