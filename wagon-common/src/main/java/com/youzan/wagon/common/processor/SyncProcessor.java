package com.youzan.wagon.common.processor;

import java.util.List;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

public interface SyncProcessor {
    boolean process(List<Entry> entries);
}
