package com.cqt.hmbc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * RestTemplate 请求类
 *
 * @author Xienx
 * @date 2023年02月23日 11:11
 */
@Slf4j
@SuppressWarnings("unused")
public class RestTemplateRequest {
    /**
     * 接口请求地址
     */
    private String url;
    /**
     * 接口请求超时时间, 默认不超时
     */
    private Integer timeout;

    /**
     * 请求头
     */
    private HttpHeaders headers;

    /**
     * restTemplate 实例
     */
    private final RestTemplate restTemplate;
    /**
     * 请求方法
     */
    private HttpMethod method = HttpMethod.GET;
    /**
     * 存储表单数据
     */
    private final Map<String, Object> form;

    /**
     * body 请求体
     */
    private String body;

    public RestTemplateRequest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.headers = new HttpHeaders();
        form = new HashMap<>();
        timeout = null;
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }

    public static RestTemplateRequest of(RestTemplate restTemplate) {
        Assert.notNull(restTemplate, "restTemplate must be not null!");
        return new RestTemplateRequest(restTemplate);
    }

    public RestTemplateRequest method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public RestTemplateRequest url(String url) {
        this.url = url;
        return this;
    }


    public RestTemplateRequest headers(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public RestTemplateRequest body(String body) {
        this.body = body;
        return this;
    }

    public <T> RestTemplateRequest form(Map<String, T> params) {
        if (params != null && !params.isEmpty()) {
            this.form.putAll(params);
        }
        return this;
    }

    public <T> RestTemplateRequest form(String name, T value) {
        this.form.put(name, value);
        return this;
    }

    /**
     * 设置接口超时时间, 目前该配置还无法生效
     *
     * @param timeout 接口超时时间, 单位毫秒
     */
    public RestTemplateRequest timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public RestTemplateRequest get(String url) {
        return url(url)
                .method(HttpMethod.GET);
    }

    public RestTemplateRequest head(String url) {
        return url(url)
                .method(HttpMethod.HEAD);
    }

    public RestTemplateRequest post(String url) {
        return url(url)
                .method(HttpMethod.POST);
    }

    public RestTemplateRequest put(String url) {
        return url(url)
                .method(HttpMethod.PUT);
    }

    public RestTemplateRequest patch(String url) {
        return url(url)
                .method(HttpMethod.PATCH);
    }

    public RestTemplateRequest delete(String url) {
        return url(url)
                .method(HttpMethod.DELETE);
    }

    public RestTemplateRequest options(String url) {
        return url(url)
                .method(HttpMethod.OPTIONS);
    }

    public RestTemplateRequest trace(String url) {
        return url(url)
                .method(HttpMethod.TRACE);
    }

    public ResponseEntity<String> execute() throws RestClientException {
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity;
        if (form.isEmpty()) {
            log.info("{}: {}", method.name(), url);
            log.info("bodyParam : {}", body);
            responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
        } else {
            log.info("{}: {}  {}", method.name(), url, form);
            responseEntity = restTemplate.exchange(url, method, requestEntity, String.class, form);
        }
        log.info("responseEntity: {}", responseEntity);

        return responseEntity;
    }
}
