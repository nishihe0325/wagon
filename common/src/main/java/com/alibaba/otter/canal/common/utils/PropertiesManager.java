package com.alibaba.otter.canal.common.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wangguofeng since 2016年3月30日 下午6:22:46
 */
public class PropertiesManager {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesManager.class);

    private static String CONFIG = "/wagon.properties";
    private static Properties properties;
    private static final AtomicLong LAST_MODIFIED = new AtomicLong();
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, new NamedThreadFactory("PropertiesManagerScheduler"));

    public static void start() throws Exception {
        load();// 加载配置文件

        EXECUTOR.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    load();
                } catch (Throwable e) {
                    LOG.error("Loading config failed:{}, {}", CONFIG, ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public static void stop() throws IOException {
        EXECUTOR.shutdown();
    }

    private static void load() throws IOException, URISyntaxException {
        File file = new File(PropertiesManager.class.getResource(CONFIG).toURI());
        if (properties == null || file.lastModified() != LAST_MODIFIED.get()) { // 第一次加载，或有修改
            properties = new Properties();
            properties.load(new FileInputStream(file));
            LAST_MODIFIED.set(file.lastModified()); // 更新为最新修改时间
            LOG.info("reload config file [{}] completed.", CONFIG);
        }
    }

    public static void configFile(String filePath) {
        CONFIG = filePath;
    }

    public static long lastModified() {
        return LAST_MODIFIED.get();
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getConfigFileName() {
        return CONFIG;
    }

    // ================== get method ===================
    public static String getProperty(String key) {
        return StringUtils.trim(PropertiesManager.properties.getProperty(StringUtils.trim(key)));
    }

    public static String getProperty(String key, String def) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? def : value;
    }

    public static String get(String key) {
        return getProperty(key);
    }

    public static String get(String key, String def) {
        return getProperty(key, def);
    }

    public static Integer getInteger(String key) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? null : Integer.valueOf(value);
    }

    public static Integer getInteger(String key, Integer def) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? def : Integer.valueOf(value);
    }

    public static Long getLong(String key) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? null : Long.valueOf(value);
    }

    public static Long getLong(String key, Long def) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? def : Long.valueOf(value);
    }

    public static Boolean getBoolean(String key) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? null : Boolean.valueOf(value);
    }

    public static boolean getBoolean(String key, boolean def) {
        String value = getProperty(key);
        return StringUtils.isBlank(value) ? def : Boolean.valueOf(value);
    }

    // ================== get method 指定Properties 参数===================
    public static String getProperty(Properties prop, String key) {
        return StringUtils.trim(prop.getProperty(StringUtils.trim(key)));
    }

    public static String getProperty(Properties prop, String key, String def) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? def : value;
    }

    public static Integer getInteger(Properties prop, String key) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? null : Integer.valueOf(value);
    }

    public static Integer getInteger(Properties prop, String key, Integer def) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? def : Integer.valueOf(value);
    }

    public static Long getLong(Properties prop, String key) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? null : Long.valueOf(value);
    }

    public static Long getLong(Properties prop, String key, Long def) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? def : Long.valueOf(value);
    }

    public static Boolean getBoolean(Properties prop, String key) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? null : Boolean.valueOf(value);
    }

    public static boolean getBoolean(Properties prop, String key, boolean def) {
        String value = getProperty(prop, key);
        return StringUtils.isBlank(value) ? def : Boolean.valueOf(value);
    }

}
