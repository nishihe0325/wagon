package com.youzan.wagon.filter.matcher;

public class TopicMatcherFactory {

    private static TopicMatcher matcher;

    private TopicMatcherFactory() {
    }

    public static TopicMatcher getTopicMatcher() {
        if (matcher == null) {
            matcher = new TopicMatcherImpl();
        }

        return matcher;
    }

}
