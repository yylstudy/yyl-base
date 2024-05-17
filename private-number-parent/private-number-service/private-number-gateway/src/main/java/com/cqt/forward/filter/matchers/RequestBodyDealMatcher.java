package com.cqt.forward.filter.matchers;

import cn.hutool.core.util.StrUtil;
import com.cqt.forward.filter.BindRequestContext;
import com.cqt.forward.util.GatewayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Scope;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 请求体参数处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestBodyDealMatcher implements ElementMatcher<BindRequestContext> {

    private final GatewayUtil gatewayUtil;

    @Override
    public boolean matches(BindRequestContext context) {
        GatewayFilterChain chain = context.getChain();
        ServerWebExchange exchange = context.getExchange();
        ServerHttpRequest request = exchange.getRequest();

        // 请求体参数
        String requestBody = gatewayUtil.getRequestBody(exchange);
        if (StrUtil.isEmpty(requestBody)) {
            log.error("获取请求体参数为空, url: {}", request.getURI());
            context.setResult(chain.filter(exchange));
            return false;
        }
        // 企业id
        String vccId = gatewayUtil.getVccId(exchange);

        // 设置业务模式
        Optional<String> businessTypeOptional = gatewayUtil.getBusinessType(context.getPath());
        businessTypeOptional.ifPresent(context::setBusinessType);

        // 请求体 json对象
        context.setVccId(vccId);
        context.setRequestBody(requestBody);
        return true;
    }
}
