package com.youzan.wagon.persistent.dao;

import com.youzan.wagon.persistent.model.CanalInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface CanalInstanceDao {

    String COMMON_COLMUNS = "service_id, service_name, instance_name, slave_id, db_host, db_host_name, db_port, db_username, db_password, binlog_file, binlog_offset, binlog_exe_time, create_time, modify_time";

    // ==================== insert ====================
    @Insert("INSERT INTO canal_instance (" + COMMON_COLMUNS + ") VALUES (#{serviceId}, #{serviceName}, #{instanceName}, #{slaveId}, #{dbHost}, #{dbHostName}, #{dbPort}, #{dbUsername}, #{dbPassword}, #{binlogFile}, #{binlogOffset}, #{binlogExeTime}, now(), now())")
    long insert(CanalInstance canalInstance);

    // ==================== find ====================
    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_instance")
    List<CanalInstance> findAll();

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_instance WHERE id=#{0}")
    CanalInstance findById(long id);

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_instance WHERE service_Id=#{0}")
    List<CanalInstance> findByServiceId(long serviceId);

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_instance WHERE service_name=#{0}")
    List<CanalInstance> findByServiceName(String serviceName);

    @Select("SELECT id, " + COMMON_COLMUNS + " FROM canal_instance WHERE service_name=#{0} AND instance_name=#{1}")
    CanalInstance findByServiceNameAndInstanceName(String serviceName, String instanceName);

    // ==================== update ====================
    @Update("UPDATE canal_instance SET slave_id=#{slaveId}, db_host=#{dbHost}, db_host_name=#{dbHostName}, db_port=#{dbPort}, binlog_file=#{binlogFile}, binlog_offset=#{binlogOffset}, binlog_exe_time=#{binlogExeTime}, modify_time = now() WHERE service_name=#{serviceName} AND instance_name=#{instanceName}")
    boolean updateByServiceNameAndInstanceName(CanalInstance canalInstance);

    @Update("UPDATE canal_instance SET binlog_file=null, binlog_offset=null, binlog_exe_time=null, modify_time = now() WHERE service_name=#{serviceName}")
    boolean emptyBinlogPositionByServiceName(String serviceName);

    // ==================== delete ====================
    @Delete("DELETE FROM canal_instance WHERE id=#{0}")
    long deleteById(long id);

}
