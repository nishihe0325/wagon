package com.youzan.wagon.filter.matcher;

import com.youzan.wagon.common.rule.TopicRule;
import com.youzan.wagon.filter.bean.RowDataBean;

public interface TopicMatcher {

    /**
     * 判断该记录(某一行)是否符合该规则
     * 
     * @param topicRule
     * @param bean
     * @return
     */
    boolean isMatched(TopicRule topicRule, RowDataBean bean);

}
