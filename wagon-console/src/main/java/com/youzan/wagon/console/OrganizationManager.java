package com.youzan.wagon.console;

import com.alibaba.otter.canal.common.utils.HttpClientUtil;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.youzan.wagon.console.bean.UserDetail;
import com.youzan.wagon.console.bean.UserSimple;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangguofeng since 2016年6月24日 上午11:07:46
 */
@Component("organizationManager")
public class OrganizationManager {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationManager.class);

    private static final String PROPERTY_KEY_OA_URL = "watchman.oa.address";
    private static final String PROPERTY_KEY_OA_AUTH = "watchman.oa.auth";
    // private static final String PROPERTY_KEY_CAS_USER_URL =
    // "/api/v1/departments/tree";
    private static final String PROPERTY_KEY_CAS_USER_URL = "/api/v1/user/username/";

    private static final long expireInMinute = 60;

    private ConcurrentHashMap<String/* cas name */, UserDetail> userTable = new ConcurrentHashMap<String, UserDetail>();

    /**
     * 根据cas用户名，获取改用户详细信息(包括所在部门和部门其他成员信息等)。先读缓存，没有(或过期)则从oa系统读取
     *
     * @param username
     * @return
     * @throws Exception
     */
    public UserDetail getUserDetail(String username) {
        UserDetail userDetail = null;
        try {
            userDetail = userTable.get(username);
            if (userDetail == null || (System.currentTimeMillis() - userDetail.getLastUpdateDate()) > expireInMinute * 1000 * 100) {
                userDetail = getUserDetailFromOA(username); // 从oa获取下
                if (userDetail == null) {
                    userTable.remove(username); // 删除缓存
                } else {
                    userTable.put(username, userDetail); // 放入缓存
                }
            }
        } catch (Exception e) {
            LOG.error("getDeptName for [{}] error:{}", username, ExceptionUtils.getFullStackTrace(e));
        }

        return userDetail;
    }

    /**
     * 获取指定用户所属部门名称
     *
     * @param username
     * @return
     */
    public String getDepartmentName(String username) {
        try {
            UserDetail userDetail = getUserDetail(username);
            String department = userDetail == null ? "" : userDetail.getData().getDepartment_detail().getName();
            return department != null ? department : "";
        } catch (Exception e) {
            LOG.error("getDepartmentName failed, cause by:{}", ExceptionUtils.getFullStackTrace(e));
            return "";
        }
    }

    /**
     * 获取指定用户所属部门成员名称集合
     *
     * @param username
     * @return
     */
    public String[] getDeptUsernameArray(String username) {
        UserDetail userDetail = getUserDetail(username);
        if (userDetail != null) {
            List<UserSimple> userSimpleList = userDetail.getData().getDepartment_detail().getUsers();
            if (userSimpleList != null && userSimpleList.size() > 0) {
                String[] array = new String[userSimpleList.size()];
                for (int i = 0; i < userSimpleList.size(); i++) {
                    array[i] = userSimpleList.get(i).getUsername();
                }
                return array;
            }
        }

        return null;
    }

    /**
     * 根据cas用户名，从oa系统改用户获取详细信息
     *
     * @param username
     * @return
     * @throws Exception
     */
    private UserDetail getUserDetailFromOA(String username) throws Exception {
        try {
            String url = PropertiesManager.getProperty(PROPERTY_KEY_OA_URL) + PROPERTY_KEY_CAS_USER_URL + username;
            String auth = PropertiesManager.getProperty(PROPERTY_KEY_OA_AUTH);
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("authorization", auth);
            String json = HttpClientUtil.httpRequestByGet(url, headers); // 发送http请求
            return UserDetail.fromJson(json, UserDetail.class); // 解析返回json
        } catch (Exception e) {
            LOG.error("get UserDetail for [{}] from oa error:{}", username, ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
    }

    public void updateFromOA() throws Exception {
    }

}
