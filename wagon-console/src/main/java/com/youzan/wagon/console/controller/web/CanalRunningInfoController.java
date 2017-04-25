package com.youzan.wagon.console.controller.web;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.otter.canal.extend.common.bean.CanalInstanceRunningData;
import com.youzan.wagon.console.CanalRunningDataFormater;
import com.youzan.wagon.persistent.model.CanalRunningInfoWrapper;
import com.youzan.wagon.persistent.mvc.service.CanalRunningStatusMvcService;

/**
 * Canal运行信息控制器
 * 
 * @author wangguofeng since 2016年3月9日 下午2:40:59
 */
@Controller
public class CanalRunningInfoController {

    private static final Logger LOG = LoggerFactory.getLogger(CanalRunningInfoController.class);

    // MVC URL
    private static final String URL_CANAL_RUNNING_INFO_LIST = "/canalRunningInfoList";
    private static final String URL_CANAL_RUNNING_INFO_DETAIL = "/canalRunningInfoDetail";

    // VM name
    private static final String VM_CANAL_RUNNING_INFO_LIST = "canalRunningInfoList";
    private static final String VM_CANAL_RUNNING_INFO_DETAIL = "canalRunningInfoDetail";

    @Resource
    private CanalRunningStatusMvcService canalRunningInfoService;

    @RequestMapping(value = URL_CANAL_RUNNING_INFO_LIST, method = RequestMethod.GET)
    public String canalRunningInfoList(Model model) {
        String resultMsg = null;

        try {
            Map<String, Object> condition = new HashMap<String, Object>();
            CanalRunningInfoWrapper wrapper = canalRunningInfoService.canalRunningInfoList(condition);
            model.addAttribute("runningDataMap", wrapper);
            model.addAttribute("formater", CanalRunningDataFormater.buildInstance());
        } catch (Exception e) {
            LOG.error("canalRunningInfoList failed:\n{}", ExceptionUtils.getFullStackTrace(e));
            resultMsg = "query failed!";
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_RUNNING_INFO_LIST;
    }

    /**
     * canal实例查询功能
     * 
     * @param model
     * @param canalHost
     * @param canalPort
     * @param destination
     * @param dbHost
     * @param dbPort
     * @param limit
     * @param jobButton
     * @return
     */
    @RequestMapping(value = URL_CANAL_RUNNING_INFO_LIST, method = RequestMethod.POST)
    public String canalRunningInfoList(//
            Model model, //
            String canalHost,//
            Integer canalPort,//
            String destination,//
            String dbHost,//
            Integer dbPort,//
            String limit, //
            String jobButton) {
        return null;
    }

    @RequestMapping(value = URL_CANAL_RUNNING_INFO_DETAIL, method = RequestMethod.GET)
    public String canalRunningInfoDetail(Model model, String canalHost, int canalPort, String destination) {
        String resultMsg = null;

        try {
            CanalInstanceRunningData data = canalRunningInfoService.canalRunningInfoDetail(canalHost, null, canalPort, null);
            model.addAttribute("instanceData", data);
            model.addAttribute("formater", CanalRunningDataFormater.buildInstance());
        } catch (Exception e) {
            LOG.error("canalRunningInfoDetail failed:\n{}", ExceptionUtils.getFullStackTrace(e));
            resultMsg = "query failed!";
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_RUNNING_INFO_DETAIL;
    }

}
