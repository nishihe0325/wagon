package com.youzan.wagon.console.controller.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.persistent.model.RuleBizInfo;
import com.youzan.wagon.persistent.service.RuleBizInfoService;
import com.youzan.wagon.persistent.service.RuleInfoService;

@Controller
public class RuleBizController {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    // ====================@Resource=====================
    @Resource()
    private RuleBizInfoService ruleBizInfoService;
    @Resource()
    private RuleInfoService ruleInfoService;

    // ====================action=====================
    @RequestMapping(value = WagonConstants.URL_RULE_BIZ_LIST, method = RequestMethod.GET)
    public String ruleBizList(Model model, RuleBizInfo ruleBizInfo) {
        return queryRuleBiz(model, ruleBizInfo);
    }

    @RequestMapping(value = WagonConstants.URL_RULE_BIZ_LIST, method = RequestMethod.POST)
    public String queryRuleBiz(Model model, RuleBizInfo ruleBizInfo) {
        if (ruleBizInfo == null) {
            ruleBizInfo = new RuleBizInfo();
        }
        model.addAttribute(WagonConstants.MODEL_RULE_BIZ_INFO, ruleBizInfo); // 保存查询条件

        try {
            List<RuleBizInfo> ruleBizInfos = ruleBizInfoService.findAll();
            model.addAttribute(WagonConstants.MODEL_RULE_BIZ_INFOS, ruleBizInfos != null ? ruleBizInfos : new ArrayList<RuleBizInfo>());
        } catch (Exception e) {
            LOG_ERROR.error("queryRuleBiz failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
        }
        return WagonConstants.VM_RULE_BIZ_LIST;
    }

    @RequestMapping(value = WagonConstants.URL_ADD_RULE_BIZ, method = RequestMethod.GET)
    public String addRuleBiz(Model model, RuleBizInfo ruleBizInfo) {
        model.addAttribute(WagonConstants.MODEL_RULE_BIZ_INFO, ruleBizInfo); // 保存输入信息
        return WagonConstants.VM_ADD_RULE_BIZ;
    }

    @RequestMapping(value = WagonConstants.URL_ADD_RULE_BIZ, method = RequestMethod.POST)
    public String saveRuleBiz(Model model, RuleBizInfo ruleBizInfo) {
        if (ruleBizInfo == null) {
            ruleBizInfo = new RuleBizInfo();
        }
        if (StringUtils.isBlank(ruleBizInfo.getVersion())) {
            ruleBizInfo.setVersion(WagonConstants.DATE_TIME_FORMAT_PURE.format(new Date()));
        }

        try {
            // 输入合法性检查，不合法，则重新跳转到新建界面(重新输入)
            String errMsg = check(ruleBizInfo);
            if (StringUtils.isNotBlank(errMsg)) {
                model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
                return addRuleBiz(model, ruleBizInfo);
            }

            // 添加作业
            ruleBizInfoService.insert(ruleBizInfo);
            return ruleBizList(model, new RuleBizInfo()); // 新建成功，跳转到列表界面
        } catch (Exception e) {
            LOG_ERROR.error("saveRuleBiz failed:{}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return addRuleBiz(model, ruleBizInfo); // 重新跳转到新建界面(重新输入)
        }
    }

    @RequestMapping(value = WagonConstants.URL_REMOVE_RULE_BIZ, method = RequestMethod.GET)
    public String removeRuleBiz(Model model, Long id) throws IOException {
        try {
            RuleBizInfo ruleBizInfo = ruleBizInfoService.findById(id);
            if (ruleBizInfo != null && CollectionUtils.isNotEmpty(ruleInfoService.findByBizName(ruleBizInfo.getBizName()))) {
                model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "该业务尚有规则未删除，请先删除规则.");
                return ruleBizList(model, null);
            }

            // 执行删除
            ruleBizInfoService.deleteById(id);
        } catch (Exception e) {
            LOG_ERROR.error("removeRuleBiz failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员.");
        }

        return ruleBizList(model, new RuleBizInfo()); // 成不成功，都跳转到列表界面
    }

    @RequestMapping(value = WagonConstants.URL_UPDATE_RULE_BIZ_VERSION, method = RequestMethod.GET)
    public String updateRuleBizVersion(Model model, Long id) throws IOException {
        try {
            ruleBizInfoService.updateVersionById(id, WagonConstants.DATE_TIME_FORMAT_PURE.format(new Date()));
        } catch (Exception e) {
            LOG_ERROR.error("updateRuleBizVersion failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员.");
        }

        return ruleBizList(model, new RuleBizInfo()); // 成不成功，都跳转到列表界面
    }

    // ========================help method ========================
    private String check(RuleBizInfo ruleBizInfo) {
        if (StringUtils.isBlank(ruleBizInfo.getBizName())) {
            return "业务名不能为空!";
        }

        if (ruleBizInfoService.findByBizName(ruleBizInfo.getBizName()) != null) {
            return "业务名[" + ruleBizInfo.getBizName() + "]已存在，不允许重复.";
        }

        return null;
    }

}
