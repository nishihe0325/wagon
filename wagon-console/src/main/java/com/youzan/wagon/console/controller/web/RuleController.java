package com.youzan.wagon.console.controller.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.youzan.wagon.common.RuleDataWrapper;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.console.RuleBeanUtil;
import com.youzan.wagon.persistent.dao.RuleLogicFieldInfoDao;
import com.youzan.wagon.persistent.model.RuleBizInfo;
import com.youzan.wagon.persistent.model.RuleFieldInfo;
import com.youzan.wagon.persistent.model.RuleInfo;
import com.youzan.wagon.persistent.model.RuleLogicFieldInfo;
import com.youzan.wagon.persistent.model.RuleOutFieldInfo;
import com.youzan.wagon.persistent.service.RuleBizInfoService;
import com.youzan.wagon.persistent.service.RuleFieldInfoService;
import com.youzan.wagon.persistent.service.RuleInfoService;
import com.youzan.wagon.persistent.service.RuleOutFieldInfoService;

/**
 * @author wangguofeng since 2016年7月6日 下午4:33:35
 */
@Controller
public class RuleController {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger(WagonConstants.LOG_NAME_ERROR);

    private final static Set<String> AllEventType = new HashSet<String>();

    static {
        AllEventType.add("INSERT");
        AllEventType.add("UPDATE");
        AllEventType.add("DELETE");
    }

    // ====================@Resource=====================
    @Resource()
    private RuleBizInfoService ruleBizInfoService;
    @Resource()
    private RuleInfoService ruleInfoService;
    @Resource()
    private RuleFieldInfoService ruleFieldInfoService;
    @Resource()
    private RuleOutFieldInfoService ruleOutFieldInfoService;
    @Resource()
    private RuleLogicFieldInfoDao ruleLogicFieldInfoDao;

    // ====================rule=====================
    @RequestMapping(value = WagonConstants.URL_RULE_LIST, method = RequestMethod.GET)
    public String ruleList(Model model, RuleInfo ruleInfo) {
        return queryRule(model, ruleInfo);
    }

    @RequestMapping(value = WagonConstants.URL_RULE_LIST, method = RequestMethod.POST)
    public String queryRule(Model model, RuleInfo ruleInfo) {
        if (ruleInfo == null) {
            ruleInfo = new RuleInfo();
        }
        model.addAttribute(WagonConstants.MODEL_RULE_INFO, ruleInfo); // 保存查询条件
        List<RuleBizInfo> allRuleBizInfos = ruleBizInfoService.findAll();

        try {
            // 初始化查询条件
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put(WagonConstants.KEY_BIZ_NAME, ruleInfo.getBizName());
            condition.put(WagonConstants.KEY_TABLE_NAME, ruleInfo.getTableName());
            condition.put(WagonConstants.KEY_TOPIC_NAME, ruleInfo.getTopicName());

            // 执行查询
            List<RuleInfo> ruleInfos = ruleInfoService.findByMap(condition);
            model.addAttribute(WagonConstants.MODEL_RULE_BIZ_INFOS, allRuleBizInfos != null ? allRuleBizInfos : new ArrayList<RuleBizInfo>());
            model.addAttribute(WagonConstants.MODEL_RULE_INFOS, ruleInfos != null ? ruleInfos : new ArrayList<RuleInfo>());
        } catch (Exception e) {
            LOG_ERROR.error("queryRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
        }

        return WagonConstants.VM_RULE_LIST;
    }

    @RequestMapping(value = WagonConstants.URL_NEW_RULE, method = RequestMethod.GET)
    public String newRule(Model model, RuleInfo ruleInfo) {
        try {
            model.addAttribute(WagonConstants.MODEL_RULE_INFO, ruleInfo != null ? ruleInfo : new RuleInfo()); // 保存上次输入信息
            List<RuleBizInfo> ruleBizInfos = ruleBizInfoService.findAll();

            // 事件类型
            List<String> selectedEventTypes = selectedEventTypes(ruleInfo.getEventType());
            List<String> noSelectedEventTypes = noSelectedEventTypes(selectedEventTypes);

            model.addAttribute(WagonConstants.MODEL_RULE_BIZ_INFOS, ruleBizInfos != null ? ruleBizInfos : new ArrayList<RuleBizInfo>());
            model.addAttribute(WagonConstants.MODEL_SELECTED_EVENT_TYPES, selectedEventTypes != null ? selectedEventTypes : new ArrayList<String>());
            model.addAttribute(WagonConstants.MODEL_NO_SELECTED_EVENT_TYPES, noSelectedEventTypes != null ? noSelectedEventTypes : new ArrayList<String>());
            return WagonConstants.VM_NEW_RULE;
        } catch (Exception e) {
            LOG_ERROR.error("newRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return ruleList(model, null);
        }
    }

    @RequestMapping(value = WagonConstants.URL_ADD_RULE, method = RequestMethod.POST)
    public String addRule(Model model, RuleInfo ruleInfo) {
        try {
            // 输入合法性检查
            String errMsg = checkRuleInfo(ruleInfo);
            if (StringUtils.isNotBlank(errMsg)) {
                model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
                return newRule(model, ruleInfo);
            }

            ruleInfoService.insert(ruleInfo);
            return ruleList(model, null);
        } catch (Exception e) {
            LOG_ERROR.error("addRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return newRule(model, ruleInfo);
        }
    }

    @RequestMapping(value = WagonConstants.URL_EDIT_RULE, method = RequestMethod.GET)
    public String editRule(Model model, Long id) {
        try {
            List<RuleBizInfo> ruleBizInfos = ruleBizInfoService.findAll();
            RuleInfo ruleInfo = ruleInfoService.findById(id);
            if (ruleInfo == null) {
                throw new RuntimeException(String.format("规则不存在，id=%s", id));
            }
            List<RuleFieldInfo> ruleFieldInfos = ruleFieldInfoService.findByRuleId(id);
            List<RuleOutFieldInfo> ruleOutFieldInfos = ruleOutFieldInfoService.findByRuleId(id);
            List<RuleLogicFieldInfo> ruleLogicFieldInfos = ruleLogicFieldInfoDao.findByRuleId(id);

            // 事件类型
            List<String> selectedEventTypes = selectedEventTypes(ruleInfo.getEventType());
            List<String> noSelectedEventTypes = noSelectedEventTypes(selectedEventTypes);

            // 是否显示条件界面和输出字段界面
            String showFieldRow = CollectionUtils.isEmpty(ruleFieldInfos) ? WagonConstants.MODEL_VALUE_FALSE : WagonConstants.MODEL_VALUE_TRUE;
            String showOutFieldRow = CollectionUtils.isEmpty(ruleOutFieldInfos) ? WagonConstants.MODEL_VALUE_FALSE : WagonConstants.MODEL_VALUE_TRUE;
            String showLogicFieldRow = CollectionUtils.isEmpty(ruleLogicFieldInfos) ? WagonConstants.MODEL_VALUE_FALSE : WagonConstants.MODEL_VALUE_TRUE;

            // 是否显示条件界面和输出字段界面
            String showConditionOrOutFieldRela = (WagonConstants.MODEL_VALUE_TRUE.equals(showFieldRow) || WagonConstants.MODEL_VALUE_TRUE.equals(showOutFieldRow)) ? WagonConstants.MODEL_VALUE_TRUE : WagonConstants.MODEL_VALUE_FALSE;

            // 人为的放入一个空对象，为了界面的统一操作
            if (CollectionUtils.isEmpty(ruleFieldInfos)) {
                ruleFieldInfos = new ArrayList<RuleFieldInfo>();
                ruleFieldInfos.add(new RuleFieldInfo());
            }
            if (CollectionUtils.isEmpty(ruleOutFieldInfos)) {
                ruleOutFieldInfos = new ArrayList<RuleOutFieldInfo>();
                ruleOutFieldInfos.add(new RuleOutFieldInfo());
            }
            if (CollectionUtils.isEmpty(ruleLogicFieldInfos)) {
                ruleLogicFieldInfos = new ArrayList<RuleLogicFieldInfo>();
                ruleLogicFieldInfos.add(new RuleLogicFieldInfo());
            }

            // 是否显示其他属性
            boolean showOtherAttr = StringUtils.isNotBlank(ruleInfo.getSchemaName());
            showOtherAttr = showOtherAttr || StringUtils.isNotBlank(ruleInfo.getSeqConsumeField());
            showOtherAttr = showOtherAttr || (ruleInfo.getMaxRetryCount() != null && ruleInfo.getMaxRetryCount() > 0);

            // 返回显示界面
            model.addAttribute(WagonConstants.MODEL_SELECTED_EVENT_TYPES, selectedEventTypes != null ? selectedEventTypes : new ArrayList<String>());
            model.addAttribute(WagonConstants.MODEL_NO_SELECTED_EVENT_TYPES, noSelectedEventTypes != null ? noSelectedEventTypes : new ArrayList<String>());
            model.addAttribute(WagonConstants.MODEL_SHOW_FIELD_ROW, showFieldRow);
            model.addAttribute(WagonConstants.MODEL_SHOW_OUT_FIELD_ROW, showOutFieldRow);
            model.addAttribute(WagonConstants.MODEL_SHOW_LOGIC_FIELD_ROW, showLogicFieldRow);
            model.addAttribute(WagonConstants.MODEL_SHOW_CONDITION_OR_OUT_FIELD_RELA, showConditionOrOutFieldRela);
            model.addAttribute(WagonConstants.MODEL_RULE_INFO, ruleInfo != null ? ruleInfo : new RuleInfo());
            model.addAttribute(WagonConstants.MODEL_RULE_BIZ_INFOS, ruleBizInfos != null ? ruleBizInfos : new ArrayList<RuleBizInfo>());
            model.addAttribute(WagonConstants.MODEL_RULE_FIELD_INFOS, ruleFieldInfos != null ? ruleFieldInfos : new ArrayList<RuleFieldInfo>());
            model.addAttribute(WagonConstants.MODEL_RULE_OUT_FIELD_INFOS, ruleOutFieldInfos != null ? ruleOutFieldInfos : new ArrayList<RuleOutFieldInfo>());
            model.addAttribute(WagonConstants.MODEL_RULE_LOGIC_FIELD_INFOS, ruleLogicFieldInfos != null ? ruleLogicFieldInfos : new ArrayList<RuleLogicFieldInfo>());
            model.addAttribute(WagonConstants.MODEL_SHOW_OTHER_ATTR, showOtherAttr);
            return WagonConstants.VM_EDIT_RULE;
        } catch (Exception e) {
            LOG_ERROR.error("editRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return ruleList(model, null);
        }
    }

    @RequestMapping(value = WagonConstants.URL_UPDATE_RULE, method = RequestMethod.POST)
    public String updateRule(Model model, RuleInfo ruleInfo) {
        try {
            // 输入合法性检查，不合法，则重新跳转到列表界面
            String errMsg = checkRuleInfo(ruleInfo);
            if (StringUtils.isNotBlank(errMsg)) {
                model.addAttribute(WagonConstants.MODEL_RESULT_MSG, errMsg);
                return editRule(model, ruleInfo.getId());
            }

            ruleInfoService.updateById(ruleInfo);
            return ruleList(model, null);
        } catch (Exception e) {
            LOG_ERROR.error("updateRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return editRule(model, ruleInfo.getId());
        }
    }

    @RequestMapping(value = WagonConstants.URL_RULE_DETAIL, method = RequestMethod.GET)
    public String ruleDetail(Model model, Long id) throws IOException {
        try {
            RuleInfo ruleInfo = ruleInfoService.findById(id);
            model.addAttribute(WagonConstants.MODEL_RULE_INFO, ruleInfo != null ? ruleInfo : new RuleInfo());
            return WagonConstants.VM_RULE_DETAIL;
        } catch (Exception e) {
            LOG_ERROR.error("ruleDetail failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
            return ruleList(model, null);
        }
    }

    @RequestMapping(value = WagonConstants.URL_DISABLE_RULE, method = RequestMethod.GET)
    public String disableRule(Model model, Long id) throws IOException {
        try {
            ruleInfoService.updateEnableState(WagonConstants.RULE_ENABLE_STATE_DISABLED, id);
        } catch (Exception e) {
            LOG_ERROR.error("disableRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
        }

        return ruleList(model, null);
    }

    @RequestMapping(value = WagonConstants.URL_ENABLE_RULE, method = RequestMethod.GET)
    public String enableRule(Model model, Long id) throws IOException {
        try {
            ruleInfoService.updateEnableState(WagonConstants.RULE_ENABLE_STATE_ENABLED, id);
        } catch (Exception e) {
            LOG_ERROR.error("enableRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
        }

        return ruleList(model, null);
    }

    @RequestMapping(value = WagonConstants.URL_REMOVE_RULE, method = RequestMethod.GET)
    public String removeRule(Model model, Long id) throws IOException {
        try {
            RuleInfo ruleInfo = ruleInfoService.findById(id);
            if (ruleInfo != null && ruleInfo.getEnableState() == WagonConstants.RULE_ENABLE_STATE_ENABLED) {
                model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "该规则在启用中，请先暂停!");
                return ruleList(model, null);
            }
            ruleInfoService.deleteById(id);
        } catch (Exception e) {
            LOG_ERROR.error("removeRule failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            model.addAttribute(WagonConstants.MODEL_RESULT_MSG, "操作异常，请联系管理员!");
        }

        return ruleList(model, null);
    }

    // ====================== rule field ==============
    @RequestMapping(value = WagonConstants.URL_ADD_OR_EDIT_FILED, method = RequestMethod.POST)
    @ResponseBody
    public String addOrEditFiled(Model model, RuleFieldInfo ruleFieldInfo) throws IOException {
        try {
            if (ruleFieldInfo.getId() == null || "add".equals(ruleFieldInfo.getAddOrEditType())) { // 新增
                ruleFieldInfoService.insert(ruleFieldInfo);
            } else { // 编辑
                ruleFieldInfoService.updateById(ruleFieldInfo);
            }
            return "success";
        } catch (Exception e) {
            LOG_ERROR.error("addOrEditFiled failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return "failed";
        }
    }

    @RequestMapping(value = WagonConstants.URL_REMOVE_RULE_FILED, method = RequestMethod.POST)
    @ResponseBody
    public String removeFiled(Model model, RuleFieldInfo ruleFieldInfo) throws IOException {
        try {
            if (ruleFieldInfo.getId() != null && !"add".equals(ruleFieldInfo.getAddOrEditType())) { // 新增
                ruleFieldInfoService.deleteById(ruleFieldInfo.getId());
            }
            return "success";
        } catch (Exception e) {
            LOG_ERROR.error("removeFiled failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return "failed!";
        }
    }

    // ====================== rule out field ==============
    @RequestMapping(value = WagonConstants.URL_ADD_OR_EDIT_OUT_FILED, method = RequestMethod.POST)
    @ResponseBody
    public String addOrEditOutFiled(Model model, RuleOutFieldInfo ruleOutFieldInfo) throws IOException {
        try {
            if (ruleOutFieldInfo.getId() == null || "add".equals(ruleOutFieldInfo.getAddOrEditType())) { // 新增
                ruleOutFieldInfoService.insert(ruleOutFieldInfo);
            } else { // 编辑
                ruleOutFieldInfoService.updateById(ruleOutFieldInfo);
            }
            return "success";
        } catch (Exception e) {
            LOG_ERROR.error("addOrEditOutFiled failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return "failed";
        }
    }

    @RequestMapping(value = WagonConstants.URL_REMOVE_OUT_FILED, method = RequestMethod.POST)
    @ResponseBody
    public String removeOutFiled(Model model, RuleOutFieldInfo ruleOutFieldInfo) throws IOException {
        try {
            if (ruleOutFieldInfo.getId() != null && !"add".equals(ruleOutFieldInfo.getAddOrEditType())) { // 新增
                ruleOutFieldInfoService.deleteById(ruleOutFieldInfo.getId());
            }
            return "success";
        } catch (Exception e) {
            LOG_ERROR.error("removeOutFiled failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return "failed";
        }
    }

    // =====================================================================================================================

    @RequestMapping(value = WagonConstants.URL_ADD_LOGIC_FILED, method = RequestMethod.POST)
    @ResponseBody
    public String addLogicFiled(Model model, RuleLogicFieldInfo ruleLogicFieldInfo) throws IOException {
        try {
            ruleLogicFieldInfoDao.insert(ruleLogicFieldInfo);
            return "success";
        } catch (Exception e) {
            LOG_ERROR.error("addOrEditOutFiled failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return "failed";
        }
    }

    @RequestMapping(value = WagonConstants.URL_REMOVE_LOGIC_FILED, method = RequestMethod.POST)
    @ResponseBody
    public String removeLogicFiled(Model model, RuleLogicFieldInfo ruleLogicFieldInfo) throws IOException {
        try {
            if (ruleLogicFieldInfo.getId() != null && !"add".equals(ruleLogicFieldInfo.getAddOrEditType())) { // 新增
                ruleLogicFieldInfoDao.deleteById(ruleLogicFieldInfo.getId());
            }
            return "success";
        } catch (Exception e) {
            LOG_ERROR.error("removeOutFiled failed: {}\n", ExceptionUtils.getFullStackTrace(e));
            return "failed";
        }
    }

    // ====================== other ==============
    @RequestMapping(value = "getRuleData", method = RequestMethod.GET)
    @ResponseBody
    public String getRuleData(String bizName) {
        List<RuleInfo> ruleInfos = ruleInfoService.findByBizName(bizName);
        List<RuleFieldInfo> fieldInfos = ruleFieldInfoService.findByBizName(bizName);
        List<RuleOutFieldInfo> outFieldInfos = ruleOutFieldInfoService.findByBizName(bizName);

        RuleDataWrapper wrapper = RuleBeanUtil.buildRuleDataWrapper(bizName, ruleInfos, fieldInfos, outFieldInfos);

        return wrapper.toJson();
    }

    @RequestMapping(value = "importRule", method = RequestMethod.GET)
    @ResponseBody
    public String importRule(String ruleFileName) {
        return "success";
    }

    // ====================== help method ==============
    private String checkRuleInfo(RuleInfo ruleInfo) {
        if (StringUtils.isBlank(ruleInfo.getBizName())) {
            return "业务名不能为空!";
        }

        if (StringUtils.isBlank(ruleInfo.getTableName())) {
            return "表名不能为空!";
        }

        if (StringUtils.isBlank(ruleInfo.getTopicName())) {
            return "主题名不能为空!";
        }

        if (StringUtils.isBlank(ruleInfo.getEventType())) {
            return "事件类型不能为空!";
        }

        return null;
    }

    private List<String> selectedEventTypes(String eventTypesStr) {
        List<String> selectedEventTypes = new ArrayList<String>();
        if (StringUtils.isBlank(eventTypesStr)) {
            return selectedEventTypes;
        }

        for (String eventType : eventTypesStr.split(",")) {
            selectedEventTypes.add(eventType.trim());
        }
        return selectedEventTypes;
    }

    private List<String> noSelectedEventTypes(List<String> selectedEventTypes) {
        List<String> noSelectedEventTypes = new ArrayList<String>();
        for (String eventType : AllEventType) {
            if (CollectionUtils.isEmpty(selectedEventTypes) || !selectedEventTypes.contains(eventType)) {
                noSelectedEventTypes.add(eventType);
            }
        }
        return noSelectedEventTypes;
    }

}
