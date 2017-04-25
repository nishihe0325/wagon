package com.youzan.wagon.filter.converter;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.filter.bean.RowDataBean;

public interface MessageConverter {

    /**
     * 将一条变更的数据库记录，根据特定的规则，转化需要处理的消息，比如，转化为需要被推送到消息队列的消息
     * 
     * @param bean
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    String conver(RowDataBean bean, TopicRule topicRule, EventType eventType, CanalEntry.Header header) throws Exception;

}
