package com.youzan.wagon.filter.processor.mq;

import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQInvalidMessageException;
import com.youzan.nsq.client.exception.NSQInvalidTopicException;
import com.youzan.nsq.client.exception.NSQTopicNotFoundException;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.processor.MQService;
import com.youzan.wagon.common.rule.TableRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.common.util.StringUtils;
import com.youzan.wagon.filter.container.RuleContainer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class RocketmqService implements MQService {
    private static final Logger LOG = LoggerFactory.getLogger(WagonConstants.LOG_NAME_NSQ);
    private static final Logger LOG_FAIL = LoggerFactory.getLogger(WagonConstants.LOG_NAME_NSQ_FAIL);
    private static final String GroupName = "DdfaultGroupName";

    private final RuleContainer ruleContainer = RuleContainer.instance();
    private final int maxRetryInterval = WagonConstants.DEFAULT_NSQ_DEFAULT_MAX_RETRY_INTERVAL; // 重试间隔最大值
    private final RocketmqProducerCache producerCache = RocketmqProducerCache.instance();

    public boolean put(Object message, TopicRule topicRule, Long shardingId) {
        return put(StringUtils.obj2Json(message), topicRule, shardingId);
    }

    public boolean put(String message, TopicRule topicRule, Long shardingId) {
        // pub成功,则直接返回
        if (putInner(topicRule.getTopicName(), message, shardingId)) {
            return true;
        }

        for (int retryCount = 1; ; retryCount++) { // 无限重试,直到符合条件才退出
            Integer maxRetryCount = null; // 每次重试都重新获取最大重试值(topic级的设置会动态更新)

            // 首先获取topic级的最大重试次数
            TableRule tableRule = ruleContainer.getTableRule(topicRule.getTableName());
            if (tableRule != null && CollectionUtils.isNotEmpty(tableRule.getTopicRuleList())) {
                for (TopicRule topic : tableRule.getTopicRuleList()) {
                    if (!topicRule.getTopicName().equals(topic.getTopicName())) {
                        continue;
                    }

                    // 同表名下有多个同名topic,则取最大重试次数不为空,且最小的值
                    if (topic.getMaxRetryCount() != null && (maxRetryCount == null || topic.getMaxRetryCount() < maxRetryCount)) {
                        maxRetryCount = topic.getMaxRetryCount();
                    }
                }
            }

            // 如果没有设置topic级的最大重试次数,则赋值为系统级最大重试次数
            if (maxRetryCount == null) {
                maxRetryCount = PropertiesManager.getInteger(WagonConstants.PRO_NSQ_RETRY_COUNT);
            }

            // 如果最终能获取到最大重试次数,且已重试次数超过该值,则返回false
            if (maxRetryCount != null && retryCount >= maxRetryCount) {
                // todo,打印失败存储的数据
                return false;
            }

            // 重试pub
            if (putWithSleep(topicRule.getTopicName(), message, shardingId, retryCount < maxRetryInterval ? retryCount : maxRetryInterval)) {
                return true;
            }
        }
    }

    private boolean putWithSleep(String topic, String message, Long shardingId, long sleepTimeInsecond) {
        if (putInner(topic, message, shardingId)) {
            return true;
        }

        // 线程休眠一段时间
        try {
            Thread.sleep(sleepTimeInsecond * 1000);
        } catch (InterruptedException e) {
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean putInner(String topic, String message, Long shardingId) {
        long start = 0;
        try {
            MQProducer producer = producerCache.getMQProducer(GroupName);
            Message msg = new Message("TopicTest", message.getBytes(RemotingHelper.DEFAULT_CHARSET));

            start = System.currentTimeMillis();
            SendResult sendResult = producer.send(msg);
            long spend = (System.currentTimeMillis() - start);

            LOG.info("MQ Pub success, spend={}, topic={}, shardingId={}, message={}", spend, topic, shardingId, message);
            return true;
        } catch (Exception e) {
            long spend = (System.currentTimeMillis() - start);
            LOG_FAIL.error("MQ Pub failed, spend={}, topic={}, shardingId={}, message={}, cause by:{}\n", spend, topic, shardingId, message, ExceptionUtils.getFullStackTrace(e));
            return false;
        }
    }

}
