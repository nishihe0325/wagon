package com.youzan.wagon.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author wangguofeng since 2016年4月1日 下午5:57:05
 */

@Component
public class SpringHelper implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringHelper.context = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        if (context == null) {
            // 需要处理
        }

        T bean = null;
        try {
            bean = (T) context.getBean(beanName);
        } catch (BeansException e) {
            throw e; // 需要处理
        }

        return bean;
    }

}
