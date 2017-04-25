package com.alibaba.otter.canal.extend.common.bean;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;

/**
 * @author wangguofeng since 2016年3月3日 上午10:16:52
 */
public class TablePositionDatas extends RemotingSerializable {

    private List<TablePositionData> postisons = new ArrayList<TablePositionData>();

    public TablePositionDatas() {
    }

    public void add(TablePositionData data) {
        postisons.add(data);
    }

    // ================== setter / getter ===================

    public List<TablePositionData> getPostisons() {
        return postisons;
    }

    public void setPostisons(List<TablePositionData> postisons) {
        this.postisons = postisons;
    }

}
