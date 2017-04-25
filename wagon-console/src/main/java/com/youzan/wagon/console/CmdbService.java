package com.youzan.wagon.console;

import com.youzan.wagon.common.WagonConstants;
import com.alibaba.otter.canal.common.utils.HttpClientUtil;
import com.alibaba.otter.canal.common.utils.PropertiesManager;
import com.youzan.wagon.console.bean.HostDetail;
import com.youzan.wagon.console.bean.HostDetailWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component("cmdbService")
public class CmdbService {
    private static final Logger LOG_ERROR = LoggerFactory.getLogger("ERROR");

    private String url;
    private String token;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String /* host name */, HostDetail> hostTable = new HashMap<String, HostDetail>();
    private Map<String /* ip */, HostDetail> ipTable = new HashMap<String, HostDetail>();

    public Collection<HostDetail> getHosts() {
        try {
            this.lock.readLock().lockInterruptibly();
            return hostTable.values();
        } catch (Exception e) { // 捕获到锁异常，不处理
            LOG_ERROR.error("getHosts failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        } finally {
            this.lock.readLock().unlock();
        }

        return new ArrayList<HostDetail>();
    }

    public Map<String, HostDetail> getIpTable() {
        try {
            this.lock.readLock().lockInterruptibly();
            return ipTable;
        } catch (Exception e) { // 捕获到锁异常，不处理
            LOG_ERROR.error("getIpTable failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        } finally {
            this.lock.readLock().unlock();
        }

        return new HashMap<String, HostDetail>();
    }

    public Map<String, HostDetail> getHostTable() {
        try {
            this.lock.readLock().lockInterruptibly();
            return hostTable;
        } catch (Exception e) { // 捕获到锁异常，不处理
            LOG_ERROR.error("getHostTable failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        } finally {
            this.lock.readLock().unlock();
        }

        return new HashMap<String, HostDetail>();
    }

    public Map<String, HostDetail> cloneIpTable() {
        Map<String, HostDetail> result = new HashMap<String, HostDetail>();
        for (Map.Entry<String, HostDetail> entry : getIpTable().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public HostDetail getHost(String hostName) {
        try {
            this.lock.readLock().lockInterruptibly();
            return hostTable.get(hostName);
        } catch (Exception e) { // 捕获到锁异常，不处理
            LOG_ERROR.error("getHost failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        } finally {
            this.lock.readLock().unlock();
        }

        return null;
    }

    public HostDetail getHostByIp(String ip) {
        try {
            this.lock.readLock().lockInterruptibly();
            return ipTable.get(ip);
        } catch (Exception e) { // 捕获到锁异常，不处理
            LOG_ERROR.error("getHostByIp failed, cause by:{}\n", ExceptionUtils.getFullStackTrace(e));
        } finally {
            this.lock.readLock().unlock();
        }

        return null;
    }

    /**
     * 从cmdb获取最新的主机信息，更新到缓存
     *
     * @throws Exception
     */
    public void updateFromCMDB() throws Exception {
        // property属性
        if (StringUtils.isBlank(this.url)) {
            this.url = PropertiesManager.getProperty(WagonConstants.PRO_CMDB_ADDR);
            if (StringUtils.isBlank(this.url)) {
                throw new RuntimeException(String.format("property [%s] cant't be blank.", WagonConstants.PRO_CMDB_ADDR));
            }
            url = url + "?per_page=100000";
        }
        if (StringUtils.isBlank(this.token)) {
            this.token = PropertiesManager.getProperty(WagonConstants.PRO_CMDB_TOKEN);
            if (StringUtils.isBlank(this.token)) {
                throw new RuntimeException(String.format("property [%s] cant't be blank.", WagonConstants.PRO_CMDB_TOKEN));
            }
        }

        // http header
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("authorizationtype", "token");
        headers.put("authorizationcode", this.token);
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");

        // send request
        String json = HttpClientUtil.httpRequestByGet(url, headers);
        HostDetailWrapper wrapper = HostDetailWrapper.fromJson(json, HostDetailWrapper.class);

        // 更新到本地缓存
        if (wrapper != null && wrapper.getData() != null && CollectionUtils.isNotEmpty(wrapper.getData().getValue())) {
            List<HostDetail> hosts = wrapper.getData().getValue();
            Map<String, HostDetail> hostTableTmp = new HashMap<String, HostDetail>();
            Map<String, HostDetail> ipTableTmp = new HashMap<String, HostDetail>();
            for (HostDetail hostDetail : hosts) {
                hostTableTmp.put(hostDetail.getName(), hostDetail);
                ipTableTmp.put(hostDetail.getDnsip(), hostDetail);
            }

            try {
                this.lock.writeLock().lockInterruptibly();
                this.hostTable = hostTableTmp;
                this.ipTable = ipTableTmp;
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

}
