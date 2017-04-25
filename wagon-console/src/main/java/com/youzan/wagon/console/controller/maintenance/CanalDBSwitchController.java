package com.youzan.wagon.console.controller.maintenance;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.youzan.wagon.persistent.mvc.service.CanalDBSwitchMvcService;

/**
 * <pre>
 * canal发起，或需要和canal交互的请求，主要有两类：
 * 
 * 1，来自canal的请求，如：
 * 1），实例注册请求；
 * 2），实例信息查询请求；
 * 3），客户端消费位点更新请求；
 * 
 * 2，来自运维的请求：
 * 1），数据库切换通知请求
 * </pre>
 * 
 * @author wangguofeng since 2016年1月8日 下午3:35:10
 */
@Controller
public class CanalDBSwitchController {

    private static final Logger LOG = LoggerFactory.getLogger(CanalDBSwitchController.class);

    // http request URL
    private static final String CANAL_DB_SWITCH_URL = "/canal/canalDBSwitch.json"; // canal对应mysql切换

    @Resource
    private CanalDBSwitchMvcService canalManageService;

    /**
     * canal数据库切换
     * 
     * @param json
     *            请求字符串，包含切换前mysql信息，切换后mysql信息，json格式的字符串
     * @return 切换结果(可能包含多个canal实例的切换结果，因为一个mysql实例可能被多个canal实例监听)，json格式的字符串
     */
    @RequestMapping(value = CANAL_DB_SWITCH_URL, method = RequestMethod.POST)
    @ResponseBody
    public String canalDBSwitch(@RequestBody String json) {
        LOG.info("receive canalDBSwitch request:{}", json);
        String result = canalManageService.canalDBSwitch(json);
        LOG.info("response canalDBSwitch result:{}", result);
        return result;
    }

}
