package com.youzan.wagon.persistent.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.CanalClientPositionInfoDao;
import com.youzan.wagon.persistent.model.CanalClientPositionInfo;
import com.youzan.wagon.persistent.service.CanalClientPositionInfoService;

/**
 * @author wangguofeng since 2016年2月18日 下午8:29:16
 */
@Component("canalClientPositionInfoService")
public class CanalClientPositionInfoServiceImpl implements CanalClientPositionInfoService {

    @Autowired
    private CanalClientPositionInfoDao dao;

    // ==================== insert ====================

    @Override
    public void insert(CanalClientPositionInfo position) {
        dao.insert(position);
    }

    // ==================== find ====================

    @Override
    public List<CanalClientPositionInfo> findAll() {
        return dao.findAll();
    }

    public CanalClientPositionInfo findById(Long id) {
        return dao.findById(id);
    }

    @Override
    public List<CanalClientPositionInfo> findByCH_CP_Dest_ClientId(CanalClientPositionInfo positionInfo) {
        return dao.findByCH_CP_Dest_ClientId(positionInfo);
    }

    @Override
    public List<CanalClientPositionInfo> findByMap(Map<String, Object> condition) {
        return dao.findByMap(condition);
    }

    // ==================== update ====================
    @Override
    public boolean updatePosition(CanalClientPositionInfo position) {
        return dao.updatePosition(position);
    }

    // ==================== delete ====================

    @Override
    public void deleteById(String id) {
        dao.deleteById(id);
    }

}
