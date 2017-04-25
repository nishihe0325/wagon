package com.youzan.wagon.console.controller.web;

import javax.annotation.Resource;

import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.persistent.model.*;
import com.youzan.wagon.persistent.service.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CanalInstanceManageController {
    private static final Logger LOG = LoggerFactory.getLogger(CanalInstanceManageController.class);
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    // ================== Resource ===================
    @Resource()
    private RuleBizInfoService ruleBizInfoService;
    @Resource()
    private CanalServiceService canalServiceService;
    @Resource()
    private CanalInstanceService canalInstanceService;
    @Resource()
    private CanalServerService canalServerService;

    // ================== 展示服务和实例信息(默认展示第一个服务) ===================
    @RequestMapping(value = WagonConstants.URL_INSTANCE_MANAGE, method = RequestMethod.GET)
    public String instanceManage(Model model, CanalService param) {
        return instanceManageByPost(model, param);
    }

    @RequestMapping(value = WagonConstants.URL_INSTANCE_MANAGE, method = RequestMethod.POST)
    public String instanceManageByPost(Model model, CanalService param) {
        if (param == null) {
            param = new CanalService();
        }
        model.addAttribute(WagonConstants.TAB_NAME, WagonConstants.TAB_NAME_INSTANCE_MANAGE);

        try {
            // 获取所有业务规则名
            List<String> allRuleBizNames = ruleBizInfoService.findAllNames();
            allRuleBizNames = allRuleBizNames != null ? allRuleBizNames : new ArrayList<String>();

            // 获取所有服务信息
            List<CanalService> allCanalService = canalServiceService.findAll();
            allCanalService = allCanalService != null ? allCanalService : new ArrayList<CanalService>();

            // 获取当前服务信息(没有指定或找不到,则使用第一个)
            CanalService curCanalService = null;
            if (StringUtils.isNotBlank(param.getServiceName())) {
                curCanalService = canalServiceService.findByServiceName(param.getServiceName());
            } else if (param.getId() != null) {
                curCanalService = canalServiceService.findById(param.getId());
            }
            if (curCanalService == null && allCanalService.size() > 0) {
                curCanalService = allCanalService.get(0);
            }
            curCanalService = curCanalService != null ? curCanalService : new CanalService();


            // 获取当前服务的所有实例
            List<CanalInstance> curAllCanalInstances = null;
            if (StringUtils.isNotBlank(curCanalService.getServiceName())) {
                curAllCanalInstances = canalInstanceService.findByServiceName(curCanalService.getServiceName());
            }
            curAllCanalInstances = curAllCanalInstances != null ? curAllCanalInstances : new ArrayList<CanalInstance>();

            List<CanalServer> curAllCanalServers = null;
            if (StringUtils.isNotBlank(curCanalService.getServiceName())) {
                curAllCanalServers = canalServerService.findByServiceName(curCanalService.getServiceName());
            }
            curAllCanalServers = curAllCanalServers != null ? curAllCanalServers : new ArrayList<CanalServer>();


            // 设置上下文
            model.addAttribute(WagonConstants.MODEL_ALL_RULE_BIZ_NAMES, allRuleBizNames);
            model.addAttribute(WagonConstants.MODEL_ALL_CANAL_SERVICES, allCanalService);
            model.addAttribute(WagonConstants.MODEL_CUR_CANAL_SERVICE, curCanalService);
            model.addAttribute(WagonConstants.MODEL_CUR_ALL_CANAL_INSTANCES, curAllCanalInstances);
            model.addAttribute(WagonConstants.MODEL_CUR_ALL_CANAL_SERVERS, curAllCanalServers);
        } catch (Throwable e) {
            LOG_ERROR.error("instanceManage failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
        }

        return WagonConstants.VM_INSTANCE_MANAGE;
    }

    // ================== 编辑新建 ===================
    @RequestMapping(value = WagonConstants.URL_NEW_SERVICE, method = RequestMethod.POST)
    public String newService(Model model, CanalService param) {
        if (param == null) {
            param = new CanalService();
        }

        // 条件检测
        String errMsg = null;
        if (errMsg == null && StringUtils.isBlank(param.getServiceName())) {
            errMsg = "服务名不能为空!";
        }
        if (errMsg == null && StringUtils.isBlank(param.getRuleBizName())) {
            errMsg = "规则业务名不能为空!";
        }
        if (errMsg == null) {
            CanalService canalService = canalServiceService.findByServiceName(param.getServiceName());
            if (canalService != null) {
                errMsg = "服务名已存在!";
            }
        }

        // 条件符合则新建
        if (errMsg == null) {
            try {
                canalServiceService.insert(param);
            } catch (Throwable e) {
                LOG_ERROR.error("newService failed: {}\n", ExceptionUtils.getFullStackTrace(e));
                errMsg = "新建失败!";
            }
        }

        model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
        return instanceManageByPost(model, param);
    }

    @RequestMapping(value = WagonConstants.URL_NEW_CANAL_INSTANCE, method = RequestMethod.GET)
    public String newCanalInstance(Model model, CanalService curCanalService) {
        try {
            curCanalService = curCanalService != null ? curCanalService : new CanalService();

            // 获取所有服务信息
            List<CanalService> allCanalService = canalServiceService.findAll();
            allCanalService = allCanalService != null ? allCanalService : new ArrayList<CanalService>();

            model.addAttribute(WagonConstants.MODEL_CUR_CANAL_SERVICE, curCanalService);
            model.addAttribute(WagonConstants.MODEL_ALL_CANAL_SERVICES, allCanalService);
            return WagonConstants.VM_NEW_CANAL_INSTANCE;
        } catch (Exception e) {
            LOG_ERROR.error("newCanalInstance failed: {}", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return instanceManageByPost(model, curCanalService);
        }
    }

    @RequestMapping(value = WagonConstants.URL_ADD_CANAL_INSTANCE, method = RequestMethod.POST)
    public String addCanalInstance(Model model, CanalInstance param) {
        // 保存查询条件
        if (param == null) {
            param = new CanalInstance();
        }

        // 条件检测
        String errMsg = null;
        if (errMsg == null && StringUtils.isBlank(param.getServiceName())) {
            errMsg = "服务名不能为空!";
        }
        if (errMsg == null && StringUtils.isBlank(param.getInstanceName())) {
            errMsg = "实例名不能为空!";
        }
        if (errMsg == null && StringUtils.isBlank(param.getDbHost())) {
            errMsg = "数据库主机不能为空!";
        }
        if (errMsg == null && param.getDbPort() == null) {
            errMsg = "数据库端口不能为空!";
        }

        // 查询对应服务
        CanalService canalService = null;
        if (errMsg == null) {
            canalService = canalServiceService.findByServiceName(param.getServiceName());
            if (canalService == null) {
                errMsg = "服务不存在!";
            }
        }
        if (canalService == null) {
            canalService = new CanalService();
            canalService.setServiceName(param.getServiceName());
        }

        // 新建
        if (errMsg == null) {
            try {
                param.setServiceId(canalService.getId());
                canalInstanceService.insert(param);
            } catch (Exception e) {
                LOG_ERROR.error("newCanalInstance failed: {}\n", ExceptionUtils.getFullStackTrace(e));
                errMsg = "新建失败";
            }
        }

        model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
        return instanceManageByPost(model, canalService);
    }

    // ================== 编辑更新 ===================
    @RequestMapping(value = WagonConstants.URL_EDIT_CANAL_INSTANCE, method = RequestMethod.GET)
    public String editCanalInstance(Model model, CanalInstance param) {
        CanalService curCanalService = null;

        try {
            // 获取所有服务信息
            CanalInstance canalInstance = canalInstanceService.findById(param.getId());
            if (canalInstance != null) {
                model.addAttribute(WagonConstants.MODEL_CUR_CANAL_INSTANCE, canalInstance);
            }

            return WagonConstants.VM_EDIT_CANAL_INSTANCE;
        } catch (Exception e) {
            LOG_ERROR.error("editCanalInstance failed: {}", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return instanceManageByPost(model, curCanalService);
        }
    }

    @RequestMapping(value = WagonConstants.URL_UPDATE_CANAL_INSTANCE, method = RequestMethod.POST)
    public String updateCanalInstance(Model model, CanalInstance param) {
        // 保存查询条件
        if (param == null) {
            param = new CanalInstance();
        }

        // 条件检测
        String errMsg = null;
        if (errMsg == null && StringUtils.isBlank(param.getDbHost())) {
            errMsg = "数据库主机不能为空!";
        }
        if (errMsg == null && param.getDbPort() == null) {
            errMsg = "数据库端口不能为空!";
        }

        // 查询对应服务
        CanalService canalService = null;
        if (errMsg == null) {
            canalService = canalServiceService.findByServiceName(param.getServiceName());
            if (canalService == null) {
                errMsg = "服务不存在!";
            }
        }
        if (canalService == null) {
            canalService = new CanalService();
            canalService.setServiceName(param.getServiceName());
        }

        // 新建
        if (errMsg == null) {
            try {
                param.setServiceId(canalService.getId());
                canalInstanceService.updateByServiceNameAndInstanceName(param);
            } catch (Exception e) {
                LOG_ERROR.error("newCanalInstance failed: {}\n", ExceptionUtils.getFullStackTrace(e));
                errMsg = "新建失败";
            }
        }

        model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
        return instanceManageByPost(model, canalService);
    }

    @RequestMapping(value = WagonConstants.URL_UPDAT_CANALINSTANCEVERSION, method = RequestMethod.GET)
    public String updatCanalInstanceVersion(Model model, CanalInstance param) {
        if (param == null) {
            param = new CanalInstance();
        }
        String errMsg = null;

        try {
            canalServiceService.updateModifyTimeByServiceName(param.getServiceName());
        } catch (Throwable e) {
            LOG_ERROR.error("updatCanalInstanceVersion failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            errMsg = "更新版本失败!";
        }

        model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
        return instanceManageByPost(model, new CanalService(param.getServiceName(), null));
    }

    // ================== 删除 ===================
    @RequestMapping(value = WagonConstants.URL_REMOVE_CANAL_INSTANCE, method = RequestMethod.GET)
    public String removeCanalInstance(Model model, CanalInstance param) {
        if (param == null) {
            param = new CanalInstance();
        }
        String errMsg = null;

        try {
            canalInstanceService.deleteById(param.getId());
        } catch (Throwable e) {
            LOG_ERROR.error("newService failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            errMsg = "新建失败!";
        }

        model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
        return instanceManageByPost(model, new CanalService(param.getServiceName(), null));
    }

    @RequestMapping(value = WagonConstants.URL_CLEAN_BINLOG_POSITION, method = RequestMethod.GET)
    public String cleanBinlogPosition(Model model, CanalInstance param) {
        String errMsg = null;
        try {
            param = param != null ? param : new CanalInstance();
            canalInstanceService.emptyBinlogPositionByServiceName(param.getServiceName());
        } catch (Throwable e) {
            LOG_ERROR.error("cleanBinlogPosition failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            errMsg = "失败!";
        }

        model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
        return instanceManageByPost(model, new CanalService(param.getServiceName(), null));
    }

    // ================== 切换数据库 ===================
    @RequestMapping(value = WagonConstants.URL_SWITCH_CANAL_INSTANCE, method = RequestMethod.POST)
    public String switchCanalInstance(Model model, CanalInstance param) {
//        return instanceManageByPost(model, param);
        return null;
    }

}
