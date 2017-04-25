package com.youzan.wagon.common;

/**
 * 后台服务线程基类
 */
public abstract class ServiceThread implements Runnable {

    private final Thread thread; // 执行线程
    private volatile boolean stoped = false; // 线程是否已经停止

    public ServiceThread() {
        this.thread = new Thread(this, this.getServiceName());
    }

    public abstract String getServiceName();

    public void start() {
        this.thread.start();
    }

    public void stop() {
        this.stoped = true;
    }

    public boolean isStoped() {
        return stoped;
    }

}
