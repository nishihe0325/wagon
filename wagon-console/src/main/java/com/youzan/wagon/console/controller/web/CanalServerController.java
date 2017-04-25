package com.youzan.wagon.console.controller.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.youzan.wagon.persistent.ConditionUtils;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.service.CanalServerInfoService;

/**
 * Canal Server查看，查询，管理的MVC控制类
 * 
 * @author wangguofeng since 2016年3月9日 下午2:40:59
 */
@Controller
public class CanalServerController {

    private static final Logger LOG = LoggerFactory.getLogger(CanalServerController.class);

    // MVC URL
    private static final String URL_CANAL_SERVER_LIST = "/canalServerList";

    // VM name
    private static final String VM_CANAL_SERVER_LIST = "canalServerList"; // canal服务列表查看界面

    @Resource()
    private CanalServerInfoService canalServerInfoService;

    /**
     * canal实例查询页
     * 
     * @param model
     * @param canalHostName
     * @param canalHost
     * @param canalPort
     * @return
     */
    @RequestMapping(value = URL_CANAL_SERVER_LIST, method = RequestMethod.GET)
    public String canalServerList(//
            Model model, //
            String canalHostName,//
            String canalHost, //
            Integer canalPort) {
        return canalServerListByPost(model, canalHostName, canalHost, canalPort);
    }

    /**
     * canal实例查询页
     * 
     * @param model
     * @param canalHostName
     * @param canalHost
     * @param canalPort
     * @return
     */
    @RequestMapping(value = URL_CANAL_SERVER_LIST, method = RequestMethod.POST)
    public String canalServerListByPost(//
            Model model, //
            String canalHostName,//
            String canalHost, //
            Integer canalPort) {
        String resultMsg = null;

        try {
            Map<String, Object> condition = new HashMap<String, Object>();
            ConditionUtils.put(condition, ConditionUtils.CANAL_HOST_NAME, canalHostName);
            ConditionUtils.put(condition, ConditionUtils.CANAL_HOST, canalHost);
            ConditionUtils.put(condition, ConditionUtils.CANAL_PORT, canalPort);

            List<CanalServerInfo> canalServers = canalServerInfoService.findByMap(condition);
            model.addAttribute("canalServers", canalServers);
        } catch (Exception e) {
            LOG.error("query canalServerList failed, canalHostName:{}, canalHost:{}, canalPort:{}, causy by: {}\n",//
                    canalHostName, canalHost, canalPort, ExceptionUtils.getFullStackTrace(e));
            resultMsg = "query failed!";
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_SERVER_LIST;
    }

}
