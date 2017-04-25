package com.youzan.wagon.deployer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {
    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    /**
     * io异常交个调用方处理
     * 
     * @param url
     * @return
     * @throws IOException
     */
    public static String sendGet(String url) throws IOException {
        String result = "";
        BufferedReader in = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                log.error("HttpClient.sentGet({}).close error at:{}", url, e);
            }
        }
        return result;
    }

}
