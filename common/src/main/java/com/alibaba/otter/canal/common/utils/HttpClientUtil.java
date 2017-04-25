package com.alibaba.otter.canal.common.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * http请求客户端
 */
public class HttpClientUtil {

    private static final int CONNECT_TIMEOUT = 3000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 10000;

    public static String httpRequestByPost(String url, String param) throws Exception {
        HttpPost httpPost = null;
        CloseableHttpClient client = null;
        try {
            // post请求
            httpPost = new HttpPost(url);
            httpPost.setConfig(buildRequestConfig());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(param, "UTF-8"));
            client = HttpClientBuilder.create().build();

            // 发送请求，并获取返回值
            HttpEntity httpEntity = client.execute(httpPost).getEntity();
            return (httpEntity == null) ? null : EntityUtils.toString(httpEntity, "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            release(httpPost, client);
        }
    }

    public static String httpRequestByGet(String url) throws Exception {
        HttpGet httpGet = null;
        CloseableHttpClient client = null;
        try {
            // get请求
            httpGet = new HttpGet(url);
            httpGet.setConfig(buildRequestConfig());
            client = HttpClientBuilder.create().build();

            // 发送请求，并获取返回值
            HttpEntity httpEntity = client.execute(httpGet).getEntity();
            return (httpEntity == null) ? null : EntityUtils.toString(httpEntity, "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            release(httpGet, client);
        }
    }

    public static String httpRequestByGet(String url, Map<String, String> headers) throws Exception {
        HttpGet httpGet = null;
        CloseableHttpClient client = null;
        try {
            // get请求
            httpGet = new HttpGet(url);
            httpGet.setConfig(buildRequestConfig());
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            client = HttpClientBuilder.create().build();

            // 发送请求，并获取返回值
            HttpEntity httpEntity = client.execute(httpGet).getEntity();
            return (httpEntity == null) ? null : EntityUtils.toString(httpEntity, "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            release(httpGet, client);
        }
    }

    private static RequestConfig buildRequestConfig() {
        RequestConfig config = RequestConfig.custom()//
                .setConnectTimeout(CONNECT_TIMEOUT)//
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)//
                .setSocketTimeout(SOCKET_TIMEOUT)//
                .build();
        return config;
    }

    /**
     * 释放资源
     *
     * @param request
     * @param httpClient
     */
    private static void release(HttpRequestBase request, CloseableHttpClient httpClient) {
        if (request != null) {
            request.abort();
        }
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

}
