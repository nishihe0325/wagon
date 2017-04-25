package com.youzan.wagon.console.bean;


import com.alibaba.otter.canal.extend.common.RemotingSerializable;

public class HostDetail extends RemotingSerializable {
    private String name;
    private String dnsip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDnsip() {
        return dnsip;
    }

    public void setDnsip(String dnsip) {
        this.dnsip = dnsip;
    }

}
