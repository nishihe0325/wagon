package com.youzan.wagon.binlog.handler;

import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author wangguofeng since 2016年5月6日 下午9:33:19
 */
public interface EntryHandler {

    void handle(CanalEntry.Entry entry) throws CanalParseException, InvalidProtocolBufferException;

}
