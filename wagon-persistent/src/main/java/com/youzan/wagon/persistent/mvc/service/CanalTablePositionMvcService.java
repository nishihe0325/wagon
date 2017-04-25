package com.youzan.wagon.persistent.mvc.service;

import java.util.List;

import com.youzan.wagon.persistent.model.TablePositionBean;

/**
 * 查询表对应的mysql信息
 * 
 * @author wangguofeng since 2016年2月25日 下午4:21:15
 */
public interface CanalTablePositionMvcService {

    /**
     * 查询指定表名所在的mysql地址信息
     * 
     * @param tableName
     *            查询的表名
     * @return 表名所在的mysql地址列表和对应的canal地址列表
     */
    String queryTablePosition(String tableName);

    /**
     * 查询指定表名所在的mysql地址信息
     * 
     * @param tableName
     *            查询的表名
     * @return 表名所在的mysql地址列表和对应的canal地址列表
     */
    List<TablePositionBean> queryTablePositionDirectory(String tableName, String queryType, String showSchemaName);

}
