package com.cqt.forward.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.TreeMap;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 绑定请求过滤器处理上下文
 */
@Data
public class BindRequestContext {

    /**
     * 响应结果
     */
    private Mono<Void> result;

    /**
     * 过滤器链
     */
    private GatewayFilterChain chain;

    /**
     * 请求上下文
     */
    private ServerWebExchange exchange;

    /**
     * uri
     */
    private String path;

    /**
     * 请求体参数
     */
    private String requestBody;

    /**
     * 请求体参数map
     */
    private TreeMap<String, Object> requestObject;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 绑定的地市编码
     */
    private String areaCode;

    /**
     * 业务模式
     */
    private String businessType;

    /**
     * http请求方法
     */
    private HttpMethod method;

    /**
     * 供应商id
     */
    private String supplierId;
}
