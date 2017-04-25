package com.youzan.wagon.console.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
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
import com.youzan.wagon.persistent.ConditionUtils;
import com.youzan.wagon.persistent.model.CanalClientPositionInfo;
import com.youzan.wagon.persistent.model.CanalServerInfo;
import com.youzan.wagon.persistent.service.CanalClientPositionInfoService;

/**
 * canal客户端消费位置查看，查询，管理的MVC控制类
 * 
 * @author wangguofeng since 2016年3月9日 下午3:07:36
 */
@Controller
public class CanalClientPositionController {

    private static final Logger LOG = LoggerFactory.getLogger(CanalClientPositionController.class);

    // MVC URL
    private static final String URL_CANAL_CLIENT_POSITION_LIST = "/canalClientPositionList";
    private static final String URL_CANAL_CLIENT_POSITION_DETAIL = "/canalClientPositionDetail";
    private static final String URL_REMOVE_POSITION = "/removePosition";

    // VM name
    private static final String VM_CANAL_CLIENT_POSITION_LIST = "canalClientPositionList"; // 客户端消费位置列表界面
    private static final String VM_CANAL_CLIENT_POSITION_DETAIL = "canalClientPositionDetail"; // 客户端消费位置详情界面

    @Resource()
    private CanalClientPositionInfoService canalClientPositionInfoService;

    /**
     * 客户端消费位置查询
     * 
     * @param model
     * @param canalHostName
     * @param canalHost
     * @param canalPort
     * @param destination
     * @param dbHost
     * @param dbPort
     * @param limit
     * @param jobButton
     * @return
     */
    @RequestMapping(value = URL_CANAL_CLIENT_POSITION_LIST, method = RequestMethod.GET)
    public String canalClientPositionList(//
            Model model, //
            String canalHostName,//
            String canalHost,//
            Integer canalPort,//
            String destination,//
            String dbHost,//
            Integer dbPort,//
            String limit, //
            String jobButton) {
        return canalClientPositionListByPost(model, canalHostName, canalHost, canalPort, destination, dbHost, dbPort, limit, jobButton);
    }

    /**
     * 客户端消费位置查询
     * 
     * @param model
     * @param canalHostName
     * @param canalHost
     * @param canalPort
     * @param destination
     * @param dbHost
     * @param dbPort
     * @param limit
     * @param jobButton
     * @return
     */
    @RequestMapping(value = URL_CANAL_CLIENT_POSITION_LIST, method = RequestMethod.POST)
    public String canalClientPositionListByPost(//
            Model model, //
            String canalHostName,//
            String canalHost,//
            Integer canalPort,//
            String destination,//
            String dbHost,//
            Integer dbPort,//
            String limit, //
            String jobButton) {
        String resultMsg = null;

        try {
            // new map
            Map<CanalServerInfo, List<CanalClientPositionInfo>> clientPositionMap = MigrateMap.makeComputingMap(new Function<CanalServerInfo, List<CanalClientPositionInfo>>() {
                public List<CanalClientPositionInfo> apply(CanalServerInfo input) {
                    return new ArrayList<CanalClientPositionInfo>();
                }
            });

            int limitNum = StringUtils.isNotBlank(limit) ? Integer.parseInt(limit) : 100;

            // condition
            Map<String, Object> condition = new HashMap<String, Object>();
            ConditionUtils.put(condition, ConditionUtils.CANAL_HOST_NAME, canalHostName);
            ConditionUtils.put(condition, ConditionUtils.CANAL_HOST, canalHost);
            ConditionUtils.put(condition, ConditionUtils.CANAL_PORT, canalPort);
            ConditionUtils.put(condition, ConditionUtils.DESTINATION, destination);
            ConditionUtils.put(condition, ConditionUtils.DB_HOST, dbHost);
            ConditionUtils.put(condition, ConditionUtils.DB_PORT, dbPort);
            ConditionUtils.put(condition, ConditionUtils.LIMIT_NUM, limitNum);

            // query
            List<CanalClientPositionInfo> positions = canalClientPositionInfoService.findAll();
            if (!CollectionUtils.isEmpty(positions)) {
                for (CanalClientPositionInfo postion : positions) {
                    CanalServerInfo key = new CanalServerInfo();
                    key.setCanalHostName(postion.getCanalHostName());
                    key.setCanalHost(postion.getCanalHost());
                    key.setCanalPort(postion.getCanalPort());
                    List<CanalClientPositionInfo> postionList = clientPositionMap.get(key);
                    postionList.add(postion);
                }
            }

            model.addAttribute("clientPositionMap", clientPositionMap);
        } catch (Exception e) {
            LOG.error("query canalInstanceList failed, canalHostName:{}, canalHost:{}, canalPort:{}, destination:{}, causy by: {}\n",//
                    canalHostName, canalHost, canalPort, destination, ExceptionUtils.getFullStackTrace(e));
            resultMsg = "query failed!";
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_CLIENT_POSITION_LIST;
    }

    /**
     * canal客户端消费位置查询页
     * 
     * @param model
     * @return
     */
    @RequestMapping(value = URL_CANAL_CLIENT_POSITION_DETAIL, method = RequestMethod.GET)
    public String canalClientPositionDetailByPost(Model model, Long id) {
        String resultMsg = null;

        try {
            CanalClientPositionInfo positionInfo = canalClientPositionInfoService.findById(id);
            model.addAttribute("positionInfo", positionInfo);
        } catch (Exception e) {
            LOG.error("canalClientPositionDetail failed:\n{}", ExceptionUtils.getFullStackTrace(e));
            resultMsg = "canalClientPositionDetail failed!";
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_CLIENT_POSITION_DETAIL;
    }

    @RequestMapping(value = URL_REMOVE_POSITION, method = RequestMethod.POST)
    public String removePosition(Model model, String id) {
        String resultMsg = null;

        try {
            canalClientPositionInfoService.deleteById(id);
        } catch (Exception e) {
            LOG.error("query removePosition failed, id:{}, causy by: {}\n", id, ExceptionUtils.getFullStackTrace(e));
            resultMsg = "failed!";
        }

        model.addAttribute("resultMsg", resultMsg);
        return VM_CANAL_CLIENT_POSITION_LIST;
    }

}
