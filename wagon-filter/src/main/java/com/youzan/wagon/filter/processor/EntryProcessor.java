package com.youzan.wagon.filter.processor;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.processor.MQService;
import com.youzan.wagon.common.processor.SyncProcessor;
import com.alibaba.otter.canal.common.utils.PropertiesManager;

import java.util.List;

public class EntryProcessor implements SyncProcessor {

    private SyncProcessor realProcessor;

    public EntryProcessor(MQService mqService) throws Throwable {
        String processorType = PropertiesManager.get(WagonConstants.PRO_PROCESSOR_TYPE);
        if (WagonConstants.PRO_PROCESSOR_TYPE_LOG.equalsIgnoreCase(processorType)) {
            realProcessor = new LogDirectProcessor();
        } else {
            realProcessor = new NsqProcessor(mqService);
        }
    }

    @Override
    public boolean process(List<CanalEntry.Entry> entries) {
        return realProcessor.process(entries);
    }

}
