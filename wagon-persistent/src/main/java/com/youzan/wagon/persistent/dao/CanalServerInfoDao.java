package com.youzan.wagon.persistent.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.provide.CanalServerInfoProvide;

/**
 * @author wangguofeng since 2016年1月7日 下午11:49:06
 */
public interface CanalServerInfoDao {

    // ==================== find ====================
    @Select("SELECT canal_host_name, canal_host, canal_port FROM canal_instance_info "//
            + "GROUP BY canal_host_name, canal_host, canal_port "//
            + "ORDER BY canal_host_name, canal_host, canal_port")
    List<CanalServerInfo> findAll();

    @SelectProvider(type = CanalServerInfoProvide.class, method = "getByMap")
    List<CanalServerInfo> findByMap(Map<String, Object> condition);

}
