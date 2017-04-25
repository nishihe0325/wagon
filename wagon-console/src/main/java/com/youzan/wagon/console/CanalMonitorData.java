package com.youzan.wagon.console;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.canal.extend.common.RemotingSerializable;
import com.alibaba.otter.canal.extend.common.bean.CanalInstanceRunningData;

/**
 * @author wangguofeng since 2016年4月12日 下午3:50:50
 */
public class CanalMonitorData extends RemotingSerializable {

    private List<CanalInstanceRunningData> datas = new ArrayList<CanalInstanceRunningData>();

    public CanalMonitorData() {
    }

    public CanalMonitorData(List<CanalInstanceRunningData> instancesDatas) {
        this.datas = instancesDatas;
    }

    public List<CanalInstanceRunningData> getDatas() {
        return datas;
    }

    public void setDatas(List<CanalInstanceRunningData> datas) {
        this.datas = datas;
    }

}
