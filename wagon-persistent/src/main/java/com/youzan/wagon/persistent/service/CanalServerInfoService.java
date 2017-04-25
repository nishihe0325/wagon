package com.youzan.wagon.persistent.service;

import java.util.List;
import java.util.Map;

import com.youzan.wagon.persistent.model.CanalServerInfo;

public interface CanalServerInfoService {

    // ==================== find ====================

    List<CanalServerInfo> findAll();

    List<CanalServerInfo> findByMap(Map<String, Object> condition);

}
