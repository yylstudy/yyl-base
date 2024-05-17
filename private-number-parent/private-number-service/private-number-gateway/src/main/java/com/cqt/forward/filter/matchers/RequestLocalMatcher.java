package com.cqt.forward.filter.matchers;

import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.forward.filter.BindRequestContext;
import com.cqt.forward.handler.RemoteHandler;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.Result;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 请求本地号码池 private-number-hmyc
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestLocalMatcher implements ElementMatcher<BindRequestContext> {

    private final GatewayUtil gatewayUtil;

    private final RemoteHandler remoteHandler;

    @Override
    public boolean matches(BindRequestContext context) {

        GatewayFilterChain chain = context.getChain();
        ServerWebExchange exchange = context.getExchange();

        String areaCode = context.getAreaCode();
        String path = context.getPath();
        String requestBody = context.getRequestBody();

        // 3. 根据area_code判断是否为本机房处理,
        boolean isCurrent = remoteHandler.isCurrentRoom(exchange, areaCode);
        if (isCurrent) {
            // 根据地市编码判断是调用本机房, 放过
            context.setResult(chain.filter(exchange));
            return false;
        }

        // 6. 服务列表随机取一个ip
        Optional<String> requestPathOptional = remoteHandler.getServer(path, gatewayUtil.getLocalServiceName());
        if (!requestPathOptional.isPresent()) {
            log.error("url: {}, 异地机房服务不可用, 请求本地机房!", path);
            context.setResult(chain.filter(exchange));
            return false;
        }
        String requestPath = requestPathOptional.get();
        
        // 7. 请求异地接口
        try {
            Mono<Void> result = requestOther(exchange, context.getMethod(), requestBody, requestPath);
            context.setResult(result);
            return false;
        } catch (Exception e) {
            log.error("请求异地机房接口: {}, 参数: {}, 异常: ", requestPath, requestBody, e);
        }

        // 8 返回接口结果, 放过
        context.setResult(chain.filter(exchange));
        return true;
    }

    /**
     * 请求异地接口
     */
    private Mono<Void> requestOther(ServerWebExchange exchange, HttpMethod method, String requestBody, String requestPath) {
        log.info("请求异地机房, url: {}", requestPath);
        gatewayUtil.setAttribute(exchange, GatewayConstant.CACHED_FORWARD_URL_KEY, requestPath);
        Result result = remoteHandler.remoteRequestBreak(exchange, Maps.newHashMap(), requestBody, requestPath, method);
        if (log.isInfoEnabled()) {
            log.info("url: {}, bindId: {}, requestId: {}, response: {}", gatewayUtil.getRequestPath(exchange),
                    gatewayUtil.getBindId(exchange), gatewayUtil.getRequestId(exchange), JSON.toJSONString(result));
        }
        return gatewayUtil.responseData(exchange, result);
    }
}
