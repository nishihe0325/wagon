package com.youzan.wagon.persistent.service.impl;

import com.youzan.wagon.persistent.dao.CanalInstanceDao;
import com.youzan.wagon.persistent.model.CanalInstance;
import com.youzan.wagon.persistent.service.CanalInstanceService;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("canalInstanceService")
public class CanalInstanceServiceImpl implements CanalInstanceService {

    @Autowired
    private CanalInstanceDao canalInstanceDao;

    // ==================== insert ====================
    @Override
    public long insert(CanalInstance canalInstance) {
        return canalInstanceDao.insert(canalInstance);
    }

    // ==================== find ====================
    @Override
    public List<CanalInstance> findAll() {
        return canalInstanceDao.findAll();
    }

    @Override
    public CanalInstance findById(long id) {
        return canalInstanceDao.findById(id);
    }

    @Override
    public List<CanalInstance> findByServiceId(long serviceId) {
        return canalInstanceDao.findByServiceId(serviceId);
    }

    @Override
    public List<CanalInstance> findByServiceName(String serviceName) {
        return canalInstanceDao.findByServiceName(serviceName);
    }

    @Override
    public CanalInstance findByServiceNameAndInstanceName(String serviceName, String instanceName) {
        return canalInstanceDao.findByServiceNameAndInstanceName(serviceName, instanceName);
    }

    // ==================== update ====================
    public boolean updateByServiceNameAndInstanceName(CanalInstance canalInstance) {
        return canalInstanceDao.updateByServiceNameAndInstanceName(canalInstance);
    }

    public boolean emptyBinlogPositionByServiceName(String serviceName) {
        return canalInstanceDao.emptyBinlogPositionByServiceName(serviceName);
    }

    // ==================== delete ====================
    @Override
    public long deleteById(long id) {
        return canalInstanceDao.deleteById(id);
    }

}
