package com.youzan.wagon.console.bean;

import java.util.List;

/**
 * @author wangguofeng since 2016年6月27日 上午10:32:03
 */
public class DepartmentDetail {

    private String id;
    private String leader_id;
    private String level;
    private String name;
    private String user_count;
    private String is_leaf;
    private String hrbp_id;
    private String parent_id;

    private UserSimple leader_detail;
    private UserSimple hrbp_detail;
    private List<UserSimple> users;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<UserSimple> getUsers() {
        return users;
    }

    public void setUsers(List<UserSimple> users) {
        this.users = users;
    }

    public String getLeader_id() {
        return leader_id;
    }

    public void setLeader_id(String leader_id) {
        this.leader_id = leader_id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public UserSimple getLeader_detail() {
        return leader_detail;
    }

    public void setLeader_detail(UserSimple leader_detail) {
        this.leader_detail = leader_detail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_count() {
        return user_count;
    }

    public void setUser_count(String user_count) {
        this.user_count = user_count;
    }

    public String getIs_leaf() {
        return is_leaf;
    }

    public void setIs_leaf(String is_leaf) {
        this.is_leaf = is_leaf;
    }

    public UserSimple getHrbp_detail() {
        return hrbp_detail;
    }

    public void setHrbp_detail(UserSimple hrbp_detail) {
        this.hrbp_detail = hrbp_detail;
    }

    public String getHrbp_id() {
        return hrbp_id;
    }

    public void setHrbp_id(String hrbp_id) {
        this.hrbp_id = hrbp_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", users = " + users + ", leader_id = " + leader_id + ", level = " + level + ", leader_detail = " + leader_detail + ", name = " + name + ", user_count = " + user_count + ", is_leaf = " + is_leaf + ", hrbp_detail = " + hrbp_detail + ", hrbp_id = " + hrbp_id + ", parent_id = " + parent_id + "]";
    }
}
