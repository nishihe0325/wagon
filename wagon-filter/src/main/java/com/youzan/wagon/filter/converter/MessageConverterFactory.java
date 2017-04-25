package com.youzan.wagon.filter.converter;

public class MessageConverterFactory {

    private static MessageConverter converter;

    private MessageConverterFactory() {
    }

    public static MessageConverter getMessageConverter() {
        if (converter == null) {
            converter = new NSQMessageConverter();
        }

        return converter;
    }

}
