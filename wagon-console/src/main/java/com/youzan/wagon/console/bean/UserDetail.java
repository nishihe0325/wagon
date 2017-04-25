package com.youzan.wagon.console.bean;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangguofeng since 2016年6月27日 上午10:26:17
 */
public class UserDetail extends RemotingSerializable {

    private String code;
    private String msg;
    private long lastUpdateDate = System.currentTimeMillis();

    private UserDetailData data;

    public UserDetailData getData() {
        return data;
    }

    public void setData(UserDetailData data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public List<String> deptUsernameList() {
        List<String> result = new ArrayList<String>();
        if ("0".equals(code) && data != null) {
            DepartmentDetail deptDetail = data.getDepartment_detail();
            if (deptDetail != null && deptDetail.getUsers() != null) {
                for (UserSimple userSimple : deptDetail.getUsers()) {
                    result.add(userSimple.getUsername());
                }
            }
        }

        return result;
    }
}
