package com.youzan.wagon.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 * 参见{@link #com.facebook.concurrency.NamedThreadFactory}
 * </pre>
 * 
 * @author caohaihong 2014年5月9日 下午12:44:26
 * @version 1.0.0
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String baseName;
    private final AtomicInteger threadNum = new AtomicInteger(0);

    public NamedThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    public Thread newThread(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setName(baseName + "-" + threadNum.getAndIncrement());
        return t;
    }

}
