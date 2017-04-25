package com.youzan.wagon.persistent.service.impl;

import com.youzan.wagon.persistent.dao.CanalServiceDao;
import com.youzan.wagon.persistent.model.CanalService;
import com.youzan.wagon.persistent.service.CanalServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("canalServiceService")
public class CanalServiceServiceImpl implements CanalServiceService {

    @Autowired
    private CanalServiceDao canalServiceDao;

    // ==================== insert ====================
    @Override
    public long insert(CanalService canalService) {
        return canalServiceDao.insert(canalService);
    }

    // ==================== find ====================
    @Override
    public List<CanalService> findAll() {
        return canalServiceDao.findAll();
    }

    @Override
    public CanalService findById(long id) {
        return canalServiceDao.findById(id);
    }

    @Override
    public CanalService findByServiceName(String serviceName) {
        return canalServiceDao.findByServiceName(serviceName);
    }

    @Override
    public List<CanalService> findByRuleBizName(String ruleBizName) {
        return canalServiceDao.findByRuleBizName(ruleBizName);
    }

    // ==================== update ====================
    @Override
    public long updateModifyTimeByServiceName(String serviceName) {
        return canalServiceDao.updateModifyTimeByServiceName(serviceName);
    }

    // ==================== delete ====================
    @Override
    public long deleteById(long id) {
        return canalServiceDao.deleteById(id);
    }

}
