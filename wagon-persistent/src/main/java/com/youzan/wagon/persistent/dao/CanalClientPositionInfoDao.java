package com.youzan.wagon.persistent.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.youzan.wagon.persistent.model.CanalClientPositionInfo;
import com.youzan.wagon.persistent.provide.CanalClientPositionInfoProvide;

/**
 * @author wangguofeng since 2016年2月18日 下午8:03:16
 */
public interface CanalClientPositionInfoDao {

    // ==================== insert ====================

    @Insert("INSERT INTO canal_client_position_info "//
            + "(canal_host_name, canal_host, canal_port, destination, client_id, journal_name, position, timestamp, create_time, modify_time) " //
            + "VALUES(#{canalHostName}, #{canalHost}, #{canalPort}, #{destination}, #{clientId}, #{journalName}, #{position}, #{timestamp}, now(), now())")
    void insert(CanalClientPositionInfo position);

    // ==================== find ====================

    @Select("SELECT * FROM canal_client_position_info")
    List<CanalClientPositionInfo> findAll();

    @Select("SELECT * FROM canal_client_position_info "//
            + "WHERE id=#{0}")
    CanalClientPositionInfo findById(Long id);

    @Select("SELECT * FROM canal_client_position_info "//
            + "WHERE canal_host = #{canalHost} and canal_port = #{canalPort} and destination = #{destination} and client_id = #{clientId}")
    List<CanalClientPositionInfo> findByCH_CP_Dest_ClientId(CanalClientPositionInfo positionInfo);

    @SelectProvider(type = CanalClientPositionInfoProvide.class, method = "getByMap")
    List<CanalClientPositionInfo> findByMap(Map<String, Object> condition);

    // ==================== update ====================

    @Update("UPDATE canal_client_position_info "//
            + "SET journal_name = #{journalName}, position = #{position}, timestamp = #{timestamp}, modify_time = now() "//
            + "WHERE canal_host = #{canalHost} and canal_port = #{canalPort} and destination = #{destination} and client_id = #{clientId}")
    @Options(useGeneratedKeys = true)
    boolean updatePosition(CanalClientPositionInfo position);

    // ==================== delete ====================
    @Select("DELETE FROM canal_client_position_info WHERE id=#{0}")
    void deleteById(String id);

}
