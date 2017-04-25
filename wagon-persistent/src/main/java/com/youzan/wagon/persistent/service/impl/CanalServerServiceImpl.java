package com.youzan.wagon.persistent.service.impl;

import com.youzan.wagon.persistent.dao.CanalServerDao;
import com.youzan.wagon.persistent.model.CanalServer;
import com.youzan.wagon.persistent.service.CanalServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("canalServerService")
public class CanalServerServiceImpl implements CanalServerService {

    @Autowired
    private CanalServerDao canalServerDao;

    // ==================== insert ====================
    @Override
    public long insert(CanalServer canalServer) {
        return canalServerDao.insert(canalServer);
    }

    // ==================== find ====================
    @Override
    public List<CanalServer> findAll() {
        return canalServerDao.findAll();
    }

    @Override
    public CanalServer findById(long id) {
        return canalServerDao.findById(id);
    }

    @Override
    public List<CanalServer> findByServiceName(String serviceName) {
        return canalServerDao.findByServiceName(serviceName);
    }

    // ==================== update ====================
    @Override
    public long updateModifyTime(long id) {
        return canalServerDao.updateModifyTime(id);
    }

    // ==================== delete ====================
    @Override
    public long deleteById(long id) {
        return canalServerDao.deleteById(id);
    }

}
