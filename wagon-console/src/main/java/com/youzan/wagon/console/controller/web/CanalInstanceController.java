package com.youzan.wagon.console.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.persistent.ConditionUtils;
import com.youzan.wagon.persistent.model.CanalInstanceInfo;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.service.CanalInstanceInfoService;

/**
 * Canal实例管理
 * 
 * @author wangguofeng since 2016年3月9日 下午2:40:59
 */
@Controller
public class CanalInstanceController {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger("ERROR");

    // ================== @Resource ===================
    @Resource()
    private CanalInstanceInfoService canalInstanceInfoService;

    // ================== action ===================
    @RequestMapping(value = WagonConstants.URL_ROOT, method = RequestMethod.GET)
    public String index(Model model) {
        return queryInstance(model, null, WagonConstants.DERAULT_PAGE_SIZE);
    }

    @RequestMapping(value = WagonConstants.URL_CANAL_INSTANCE_LIST, method = RequestMethod.GET)
    public String instanceList(Model model) {
        return queryInstance(model, null, WagonConstants.DERAULT_PAGE_SIZE);
    }

    @RequestMapping(value = WagonConstants.URL_CANAL_INSTANCE_LIST, method = RequestMethod.POST)
    public String queryInstance(Model model, CanalInstanceInfo param, Integer pageSize) {
        // 保存查询条件
        if (param == null) {
            param = new CanalInstanceInfo();
        }
        model.addAttribute(WagonConstants.KEY_CANAL_HOST_NAME, param.getCanalHostName());
        model.addAttribute(WagonConstants.KEY_CANAL_HOST, param.getCanalHost());
        model.addAttribute(WagonConstants.KEY_CANAL_PORT, param.getCanalPort());
        model.addAttribute(WagonConstants.KEY_DESTINATION, param.getDestination());
        model.addAttribute(WagonConstants.KEY_DB_HOST, param.getDbHost());
        model.addAttribute(WagonConstants.KEY_DB_PORT, param.getDbPort());
        model.addAttribute(WagonConstants.KEY_PAGE_SIZE, (pageSize != null && pageSize != 0) ? pageSize : WagonConstants.DERAULT_PAGE_SIZE);

        // 初始化查询条件
        Map<String, Object> condition = new HashMap<String, Object>();
        if (param != null) {
            ConditionUtils.put(condition, ConditionUtils.CANAL_HOST_NAME, param.getCanalHostName());
            ConditionUtils.put(condition, ConditionUtils.CANAL_HOST, param.getCanalHost());
            ConditionUtils.put(condition, ConditionUtils.CANAL_PORT, param.getCanalPort());
            ConditionUtils.put(condition, ConditionUtils.DESTINATION, param.getDestination());
            ConditionUtils.put(condition, ConditionUtils.DB_HOST, param.getDbHost());
            ConditionUtils.put(condition, ConditionUtils.DB_PORT, param.getDbPort());
        }

        // 执行查询
        List<CanalInstanceInfo> allInstances = null;
        try {
            allInstances = canalInstanceInfoService.findByMap(condition);
        } catch (Exception e) {
            LOG_ERROR.error("queryInstance failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return WagonConstants.VM_SYS_ERR; // 跳转到错误页面
        }

        // 放入model
        Map<CanalServerInfo, List<CanalInstanceInfo>> serverInstanceMap = null;
        if (!CollectionUtils.isEmpty(allInstances)) {
            serverInstanceMap = MigrateMap.makeComputingMap(new Function<CanalServerInfo, List<CanalInstanceInfo>>() {
                public List<CanalInstanceInfo> apply(CanalServerInfo input) {
                    return new ArrayList<CanalInstanceInfo>();
                }
            });
            for (CanalInstanceInfo info : allInstances) {
                CanalServerInfo key = new CanalServerInfo();
                key.setCanalHostName(info.getCanalHostName());
                key.setCanalHost(info.getCanalHost());
                key.setCanalPort(info.getCanalPort());
                List<CanalInstanceInfo> infoList = serverInstanceMap.get(key);
                infoList.add(info);
            }
        }
        model.addAttribute(WagonConstants.MODEL_SERVER_INSTANCE_MAP, serverInstanceMap != null ? serverInstanceMap : new HashMap<CanalServerInfo, List<CanalInstanceInfo>>());
        return WagonConstants.VM_CANAL_INSTANCE_LIST;
    }

    @RequestMapping(value = WagonConstants.URL_CANAL_INSTANCE_DETAIL, method = RequestMethod.POST)
    public String instanceDetail(Model model, Long id) {
        try {
            CanalInstanceInfo instanceInfo = canalInstanceInfoService.findById(id);
            model.addAttribute(WagonConstants.MODEL_INSTANCE_INFO, instanceInfo != null ? instanceInfo : new CanalInstanceInfo());
            return WagonConstants.VM_CANAL_INSTANCE_DETAIL;
        } catch (Exception e) {
            LOG_ERROR.error("instanceDetail failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return WagonConstants.VM_SYS_ERR; // 跳转到错误页面
        }
    }

    @RequestMapping(value = WagonConstants.URL_REMOVE_INSTANCE, method = RequestMethod.GET)
    public String removeInstance(Model model, Long id) {
        try {
            canalInstanceInfoService.deleteById(id);
            return queryInstance(model, null, WagonConstants.DERAULT_PAGE_SIZE);
        } catch (Exception e) {
            LOG_ERROR.error("removeInstance failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return WagonConstants.VM_SYS_ERR; // 跳转到错误页面
        }

    }

}
