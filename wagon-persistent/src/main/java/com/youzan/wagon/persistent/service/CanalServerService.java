package com.youzan.wagon.persistent.service;

import com.youzan.wagon.persistent.model.CanalServer;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.model.RuleInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface CanalServerService {

    // ==================== insert ====================
    long insert(CanalServer canalServer);

    // ==================== find ====================
    List<CanalServer> findAll();

    CanalServer findById(long id);

    List<CanalServer> findByServiceName(String serviceName);

    // ==================== update ====================
    long updateModifyTime(long id);

    // ==================== delete ====================
    long deleteById(long id);

}
