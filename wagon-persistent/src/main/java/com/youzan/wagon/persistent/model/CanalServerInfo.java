package com.youzan.wagon.persistent.model;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * canal服务信息，包含canal服务器主机，端口等信息
 * 
 * @author wangguofeng since 2016年3月4日 下午2:56:07
 */
public class CanalServerInfo {

    private Long id;

    private String canalHostName;
    private String canalHost;
    private Integer canalPort;

    private Date createTime;
    private Date modifyTime;

    // ================== setter / getter ===================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCanalHostName() {
        return canalHostName;
    }

    public void setCanalHostName(String canalHostName) {
        this.canalHostName = canalHostName;
    }

    public String getCanalHost() {
        return canalHost;
    }

    public void setCanalHost(String canalHost) {
        this.canalHost = canalHost;
    }

    public Integer getCanalPort() {
        return canalPort;
    }

    public void setCanalPort(Integer canalPort) {
        this.canalPort = canalPort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CanalServerInfo)) {
            return false;
        }

        CanalServerInfo other = (CanalServerInfo) obj;
        if (canalHostName == null) {
            if (other.canalHostName != null) {
                return false;
            }
        } else if (!canalHostName.equals(other.canalHostName)) {
            return false;
        }

        if (canalHost == null) {
            if (other.canalHost != null) {
                return false;
            }
        } else if (!canalHost.equals(other.canalHost)) {
            return false;
        }

        if (canalPort == null) {
            if (other.canalPort != null) {
                return false;
            }
        } else if (!canalPort.equals(other.canalPort)) {
            return false;
        }

        return true;
    }

    /**
     * 简单实现
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        if (StringUtils.isNotBlank(canalHost)) {
            hashCode = hashCode * canalHost.hashCode();
        }
        if (canalPort != null) {
            hashCode = hashCode * canalPort.hashCode();
        }

        return hashCode;
    }

}
