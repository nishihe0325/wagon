package com.youzan.wagon.common.processor;

import com.youzan.wagon.common.rule.TopicRule;

public interface MQService {

    public boolean put(String message, TopicRule topicRule, Long shardingId);

    public boolean put(Object message, TopicRule topicRule, Long shardingId);

}
