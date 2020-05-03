package com.nan.basic.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HttpUtils {
    private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    private static final int MAX_RETRY = 2;

    private static final RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
    private static final CloseableHttpClient DEFAULT_CLIENT = HttpClients.custom().setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG).build();

    public JSONObject get(String url, String path, Map<String, String> params) {
        String totalUrl = contactUrl(url, path, params);
        HttpGet get = new HttpGet(totalUrl);
        log.info("do http get, url: {}", totalUrl);
        try {
            String body = doGetWithRetry(get);
            log.info("do http get success, url: {}, response.subString(50): {}", totalUrl, body.substring(0, 50));
            return JSON.parseObject(body);
        } catch (Exception e) {
            log.error("do http get failed! url: {}, exception: {}", totalUrl, e.toString());
            return null;
        }
    }

    private String contactUrl(String url, String path, Map<String, String> params) {
        if (!url.startsWith("http")) {
            throw new RuntimeException(String.format("协议缺失, url: %s", url));
        }

        StringBuilder result = new StringBuilder();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        result.append(url);

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        result.append(path);

        if (MapUtils.isNotEmpty(params)) {
            result.append("?");
            String paramStr = params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&&"));
            result.append(paramStr);
        }

        return result.toString();
    }

    private String doGetWithRetry(HttpGet get) throws IOException {
        int retry = 0;
        while (true) {
            try {
                return parseBodyFromResponse(DEFAULT_CLIENT.execute(get));
            } catch (IOException e) {
                if (++retry > MAX_RETRY) {
                    throw e;
                }
            }
        }
    }

    private String parseBodyFromResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            log.error("do http operation error, response: " + response);
            throw new RuntimeException(String.format("request failed! response status code: %d", statusCode));
        }
        return EntityUtils.toString(response.getEntity());
    }

    public JSONObject post(String url, String path, String payload) {
        String totalUrl = contactUrl(url, path, null);
        HttpPost post = new HttpPost(totalUrl);
        log.info("do http post, url: {}, payload: {}", totalUrl, payload);
        try {
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setHeader("Accept", "application/json;charset=UTF-8");
            StringEntity entity = new StringEntity(payload, "UTF-8");
            entity.setContentEncoding("UTF-8");
            post.setEntity(entity);
            String body = parseBodyFromResponse(DEFAULT_CLIENT.execute(post));
            log.info("do http post success, url: {}, response: {}", totalUrl, body);
            return JSON.parseObject(body);
        } catch (Exception e) {
            log.error("do http post failed! url: {}, payload: {}, exception: {}", totalUrl, payload, e.toString());
            return null;
        }
    }

    public JSONObject put(String url, String path, String payload) {
        String totalUrl = contactUrl(url, path, null);
        HttpPut put = new HttpPut(totalUrl);
        log.info("do http put, url: {}, payload: {}", totalUrl, payload);
        try {
            put.setHeader("Content-Type", "application/json");
            put.setHeader("Accept", "application/json");
            HttpEntity entity = new StringEntity(payload);
            put.setEntity(entity);
            String body = parseBodyFromResponse(DEFAULT_CLIENT.execute(put));
            log.info("do http put success, url: {}, response: {}", totalUrl, body);
            return JSON.parseObject(body);
        } catch (Exception e) {
            log.error("do http put failed! url: {}, payload: {}, exception: {}", totalUrl, payload, e.toString());
            return null;
        }
    }
}
