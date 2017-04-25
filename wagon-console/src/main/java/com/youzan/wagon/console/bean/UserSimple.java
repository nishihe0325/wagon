package com.youzan.wagon.console.bean;

/**
 * @author wangguofeng since 2016年6月24日 下午5:28:20
 */
public class UserSimple {

    private String id;
    private String username;
    private String sex;
    private String nickname;
    private String realname;
    private String avatar;
    private String position_str;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPosition_str() {
        return position_str;
    }

    public void setPosition_str(String position_str) {
        this.position_str = position_str;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", username = " + username + ", sex = " + sex + ", nickname = " + nickname + ", realname = " + realname + ", avatar = " + avatar + ", position_str = " + position_str + "]";
    }
}
