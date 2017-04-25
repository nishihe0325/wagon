package com.youzan.wagon.persistent.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.CanalInstanceInfoDao;
import com.youzan.wagon.persistent.model.CanalInstanceInfo;
import com.youzan.wagon.persistent.service.CanalInstanceInfoService;

/**
 * @author wangguofeng since 2016年1月7日 下午11:53:40
 */
@Component("canalInstanceInfoService")
public class CanalInstanceInfoServiceImpl implements CanalInstanceInfoService {

    @Autowired
    private CanalInstanceInfoDao canalInstanceInfoDao;

    // ==================== insert ====================

    @Override
    public void insert(CanalInstanceInfo info) {
        canalInstanceInfoDao.insert(info);
    }

    // ==================== find ====================

    @Override
    public List<CanalInstanceInfo> findAll() {
        return canalInstanceInfoDao.findAll();
    }

    public CanalInstanceInfo findById(Long id) {
        return canalInstanceInfoDao.findById(id);
    }

    @Override
    public List<CanalInstanceInfo> findByHostAndPort(String canalHost, Integer canalPort) {
        return canalInstanceInfoDao.findByHostAndPort(canalHost, canalPort);
    }

    @Override
    public List<CanalInstanceInfo> findByHostAndPortAndDest(String canalHost, Integer canalPort, String destination) {
        return canalInstanceInfoDao.findByHostAndPortAndDest(canalHost, canalPort, destination);
    }

    @Override
    public List<CanalInstanceInfo> findByMap(Map<String, Object> condition) {
        return canalInstanceInfoDao.findByMap(condition);
    }

    // ==================== update ====================

    public boolean updateDBInfo(CanalInstanceInfo info) {
        return canalInstanceInfoDao.updateDBInfo(info);
    }

    // ==================== delete ====================

    public void deleteById(Long id) {
        canalInstanceInfoDao.deleteById(id);
    }

}
