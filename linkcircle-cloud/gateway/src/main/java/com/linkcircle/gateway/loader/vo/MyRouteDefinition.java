package com.linkcircle.gateway.loader.vo;

import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/26 11:21
 */
public class MyRouteDefinition extends RouteDefinition {
    /**
     * 路由状态
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
