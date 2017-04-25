package com.youzan.wagon.persistent.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Options;

import com.youzan.wagon.persistent.model.CanalClientPositionInfo;

/**
 * @author wangguofeng since 2016年2月18日 下午8:03:08
 */
public interface CanalClientPositionInfoService {

    // ==================== insert ====================

    void insert(CanalClientPositionInfo position);

    // ==================== find ====================

    List<CanalClientPositionInfo> findAll();

    CanalClientPositionInfo findById(Long id);

    List<CanalClientPositionInfo> findByCH_CP_Dest_ClientId(CanalClientPositionInfo positionInfo);

    List<CanalClientPositionInfo> findByMap(Map<String, Object> condition);

    // ==================== update ====================

    @Options(useGeneratedKeys = true)
    boolean updatePosition(CanalClientPositionInfo position);

    // ==================== delete ====================

    void deleteById(String id);

}
