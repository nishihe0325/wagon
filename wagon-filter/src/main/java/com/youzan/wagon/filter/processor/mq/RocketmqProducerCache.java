package com.youzan.wagon.filter.processor.mq;

import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.utils.PropertiesManager;

import java.util.HashMap;
import java.util.Map;

public class RocketmqProducerCache {

    private String namesrvAddr;
    private Map<String/*groupName*/, MQProducer> producerTable = new HashMap<String, MQProducer>();

    private static class SingletonHolder {
        private static final RocketmqProducerCache SINGLETON = new RocketmqProducerCache();
    }

    public static RocketmqProducerCache instance() {
        return SingletonHolder.SINGLETON;
    }

    public RocketmqProducerCache() {
        namesrvAddr = PropertiesManager.get("");

        producerTable = MigrateMap.makeComputingMap(new Function<String, MQProducer>() {
            public MQProducer apply(String groupName) {
                DefaultMQProducer producer = new DefaultMQProducer(groupName);
                producer.setNamesrvAddr(namesrvAddr);
                try {
                    producer.start();
                } catch (MQClientException e) {
                    // 失败是要处理
                }
                return producer;
            }
        });
    }

    public MQProducer getMQProducer(String groupName) {
        return producerTable.get(groupName);
    }

}
