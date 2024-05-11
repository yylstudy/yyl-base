package com.linkcircle.gateway.loader.vo;

import lombok.Data;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/26 11:21
 */
@Data
public class GatewayRouteVo {
    private String id;
    private String name;
    private String uri;
    private String predicates;
    private String filters;
    private Integer stripPrefix;
    private Integer retryable;
    private Integer persist;
    private Integer status;
}
