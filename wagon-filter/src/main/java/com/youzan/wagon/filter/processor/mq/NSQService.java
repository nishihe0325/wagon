package com.youzan.wagon.filter.processor.mq;

import java.util.Properties;

import com.youzan.nsq.client.configs.ConfigAccessAgent;
import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQInvalidMessageException;
import com.youzan.nsq.client.exception.NSQInvalidTopicException;
import com.youzan.nsq.client.exception.NSQTopicNotFoundException;
import com.youzan.wagon.common.WagonConstants;
import com.youzan.wagon.common.rule.TableRule;
import com.youzan.wagon.common.rule.TopicRule;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.youzan.wagon.filter.container.RuleContainer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.ProducerImplV2;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.wagon.common.processor.MQService;
import com.youzan.wagon.common.util.StringUtils;

public class NSQService implements MQService {
    private static final Logger LOG = LoggerFactory.getLogger(WagonConstants.LOG_NAME_NSQ);
    private static final Logger LOG_FAIL = LoggerFactory.getLogger(WagonConstants.LOG_NAME_NSQ_FAIL);

    private final RuleContainer ruleContainer = RuleContainer.instance();
    private final Producer producer;
    private final int maxRetryInterval = WagonConstants.DEFAULT_NSQ_DEFAULT_MAX_RETRY_INTERVAL; // 重试间隔最大值

    public NSQService(Properties properties) {
        // 设置通用配置
        final NSQConfig config = new NSQConfig();
        config.setConnectTimeoutInMillisecond(PropertiesManager.getInteger(properties, WagonConstants.PRO_NSQ_TIMEOUT, WagonConstants.DEFAULT_NSQ_CONN_TIMEOUT));
        config.setThreadPoolSize4IO(PropertiesManager.getInteger(properties, WagonConstants.PRO_NSQ_THREAD_POOL_4IO, WagonConstants.DEFAULT_NSQ_THREAD_POOLSIZE_4IO));
        config.setConnectionPoolSize(PropertiesManager.getInteger(properties, WagonConstants.PRO_NSQ_CONNECTION_POOL_SIZE, WagonConstants.DEFAULT_NSQ_CONNECTION_POOL_SIZE));
        String retryInterval = PropertiesManager.get(WagonConstants.PRO_NSQ_PUB_RETRY_INTERVAL);
        if (StringUtils.isNotBlank(retryInterval)) {
            config.setProducerRetryIntervalBaseInMilliSeconds(Integer.valueOf(retryInterval));
        }

        // 设置nsq地址
        String nsqAddr = properties.getProperty(WagonConstants.PRO_NSQ_ADDRESS);
        config.setLookupAddresses(nsqAddr);

        // 初始化producer
        producer = new ProducerImplV2(config);
    }

    public void start() throws NSQException {
        producer.start();
    }

    public void stop() {
        producer.close();
    }

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
            start = System.currentTimeMillis();
            if (shardingId == null) {
                producer.publish(message, topic);
            } else { // 局部顺序
                Message msg = Message.create(new Topic(topic), shardingId, message);
                msg.setTopicShardingIDLong(shardingId);
                producer.publish(msg);
            }
            long spend = (System.currentTimeMillis() - start);

            LOG.info("MQ Pub success, spend={}, topic={}, shardingId={}, message={}", spend, topic, shardingId, message);
            return true;
        } catch (NSQInvalidTopicException e) { // 未建topic,则只打印日志
            long spend = (System.currentTimeMillis() - start);
            LOG_FAIL.error("MQ Pub failed, spend={}, topic={}, shardingId={}, message={}, cause by:{}\n", spend, topic, shardingId, message, ExceptionUtils.getFullStackTrace(e));
            return true;
        } catch (NSQTopicNotFoundException e) { // 未建topic,则只打印日志
            long spend = (System.currentTimeMillis() - start);
            LOG_FAIL.error("MQ Pub failed, spend={}, topic={}, shardingId={}, message={}, cause by:{}\n", spend, topic, shardingId, message, ExceptionUtils.getFullStackTrace(e));
            return true;
        } catch (NSQInvalidMessageException e) { // 消息太大
            long spend = (System.currentTimeMillis() - start);
            LOG_FAIL.error("MQ Pub failed, spend={}, topic={}, shardingId={}, message={}, cause by:{}\n", spend, topic, shardingId, message, ExceptionUtils.getFullStackTrace(e));
            return true;
        } catch (Exception e) {
            long spend = (System.currentTimeMillis() - start);
            LOG_FAIL.error("MQ Pub failed, spend={}, topic={}, shardingId={}, message={}, cause by:{}\n", spend, topic, shardingId, message, ExceptionUtils.getFullStackTrace(e));
            return false;
        }
    }

}
