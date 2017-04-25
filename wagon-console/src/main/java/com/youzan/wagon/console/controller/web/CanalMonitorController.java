package com.youzan.wagon.console.controller.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.youzan.wagon.persistent.mvc.service.CanalRunningStatusMvcService;

/**
 * Canal监控信息采集控制器，运维调用
 * 
 * @author wangguofeng since 2016年4月18日 上午10:04:49
 */
@Controller
public class CanalMonitorController {

    // MVC URL
    private static final String URL = "/getMonitorData";

    @Resource
    private CanalRunningStatusMvcService canalRunningInfoService;

    @RequestMapping(value = URL, method = RequestMethod.GET)
    @ResponseBody
    public String getMonitorData() {
        return canalRunningInfoService.getMonitorData();
    }

}
