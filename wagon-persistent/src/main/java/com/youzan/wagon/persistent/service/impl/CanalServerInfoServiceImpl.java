package com.youzan.wagon.persistent.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youzan.wagon.persistent.dao.CanalServerInfoDao;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.service.CanalServerInfoService;

/**
 * @author wangguofeng since 2016年1月7日 下午11:53:40
 */
@Component("canalServerInfoService")
public class CanalServerInfoServiceImpl implements CanalServerInfoService {

    @Autowired
    private CanalServerInfoDao canalServerInfoDao;

    // ==================== find ====================

    public List<CanalServerInfo> findAll() {
        return canalServerInfoDao.findAll();
    }

    @Override
    public List<CanalServerInfo> findByMap(Map<String, Object> condition) {
        return canalServerInfoDao.findByMap(condition);
    }

}
