package com.youzan.wagon.persistent.mvc.service;

import java.util.Map;

import com.alibaba.otter.canal.extend.common.bean.CanalInstanceRunningData;
import com.youzan.wagon.persistent.model.CanalRunningInfoWrapper;

/**
 * 查询canal运行信息服务
 * 
 * @author wangguofeng since 2016年3月14日 上午10:46:33
 */
public interface CanalRunningStatusMvcService {

    /**
     * 查询符合条件的canal实例运行信息
     * 
     * @param condition
     *            查询条件集合，比如指定具体的canal地址，具体的instance，具体的mysql地址等。如果为空，
     *            则查询所有canal的运行信息
     * @return canal运行结果，其中key为canal server地址的字符串形式，如："127.0.0.1:11111"
     */
    CanalRunningInfoWrapper canalRunningInfoList(Map<String, Object> condition);

    /**
     * 获取指定canal server上指定实例的运行信息
     * 
     * @param map
     *            查询条件集合，比如指定具体的canal地址，具体的instance，具体的mysql地址等。如果为空，
     *            则查询所有canal的运行信息
     * @return canal运行结果
     */
    public CanalInstanceRunningData canalRunningInfoDetail(String canalHost, String canalHostName, //
            int canalPort, String destination);

    /**
     * 获取canal的监控数据
     * 
     * @return
     */
    String getMonitorData();

}
