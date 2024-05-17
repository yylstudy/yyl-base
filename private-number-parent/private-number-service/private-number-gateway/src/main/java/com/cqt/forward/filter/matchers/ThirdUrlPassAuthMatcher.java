package com.cqt.forward.filter.matchers;

import cn.hutool.core.collection.CollUtil;
import com.cqt.forward.filter.BindRequestContext;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.properties.ForwardProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 第三方接口不鉴权处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ThirdUrlPassAuthMatcher implements ElementMatcher<BindRequestContext> {

    private final ForwardProperties forwardProperties;

    private final GatewayUtil gatewayUtil;

    @Override
    public boolean matches(BindRequestContext context) {
        GatewayFilterChain chain = context.getChain();
        ServerWebExchange exchange = context.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        Mono<Void> result = chain.filter(exchange);

        HttpMethod method = request.getMethod();
        boolean passFilter = gatewayUtil.passFilter(request);
        if (passFilter) {
            context.setResult(result);
            return false;
        }

        String path = request.getURI().getPath();

        // 第三方url 不鉴权, 请求转发到private-number-hmyc-third服务(方太同步中间业务平台接口)
        List<String> thirdHmycUriList = forwardProperties.getThirdHmycUriList();
        if (CollUtil.isNotEmpty(thirdHmycUriList)) {
            if (thirdHmycUriList.contains(path) && gatewayUtil.changeThirdRoute(exchange)) {
                context.setResult(result);
                return false;
            }
        }
        context.setMethod(method);
        context.setPath(path);
        return true;
    }
}
