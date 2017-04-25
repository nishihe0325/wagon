package com.youzan.wagon.console.bean;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

import java.util.ArrayList;
import java.util.List;

public class HostDetailWrapper extends RemotingSerializable {
    private int code;
    private String msg;
    private Data data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data extends RemotingSerializable {
        private int total;
        private int per_page;
        private int page;
        private List<HostDetail> value = new ArrayList<HostDetail>();

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPer_page() {
            return per_page;
        }

        public void setPer_page(int per_page) {
            this.per_page = per_page;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public List<HostDetail> getValue() {
            return value;
        }

        public void setValue(List<HostDetail> value) {
            this.value = value;
        }
    }

}
