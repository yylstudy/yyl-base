package com.cqt.forward.filter;

import cn.hutool.extra.spring.SpringUtil;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.forward.filter.matchers.*;
import com.cqt.forward.util.GatewayUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 绑定请求过滤器
 */
@Slf4j
@Component
@AllArgsConstructor
public class BindRequestFilter implements GlobalFilter, Ordered {

    private final GatewayUtil gatewayUtil;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        BindRequestContext context = new BindRequestContext();
        context.setChain(chain);
        context.setExchange(exchange);
        ElementMatchers.any()
                .and(SpringUtil.getBean(ThirdUrlPassAuthMatcher.class))
                .and(SpringUtil.getBean(RequestBodyDealMatcher.class))
                .and(SpringUtil.getBean(AuthSignMatcher.class))
                .and(SpringUtil.getBean(SupplierWeightDispatchMatcher.class))
                .and(SpringUtil.getBean(RequestLocalMatcher.class))
                .matches(context);
        gatewayUtil.setAttribute(exchange, GatewayConstant.CACHED_SUPPLIER_ID_KEY, context.getSupplierId());
        return context.getResult();
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
