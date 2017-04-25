package com.youzan.wagon.persistent.mvc.service;

/**
 * 处理canal对应的数据库切换请求逻辑
 * 
 * @author wangguofeng since 2016年2月25日 下午4:21:15
 */
public interface CanalDBSwitchMvcService {

    /**
     * canal数据库切换
     * 
     * @param json
     *            请求字符串，包含切换前mysql信息，切换后mysql信息，json格式的字符串
     * @return 切换结果(可能包含多个canal实例的切换结果，因为一个mysql实例可能被多个canal实例监听)，json格式的字符串
     */
    String canalDBSwitch(String json);

}
