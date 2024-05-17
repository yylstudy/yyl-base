package com.cqt.forward.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.forward.cache.RouteDefinitionCache;
import com.cqt.forward.router.MyRouteDefinitionRouteLocator;
import com.cqt.model.bind.vo.BindTypeVO;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.ForwardProperties;
import com.cqt.model.corpinfo.dto.SupplierWeight;
import com.cqt.model.log.GatewayLog;
import io.netty.util.NetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author linshiqiang
 * @date 2022/2/7 13:30
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayUtil {

    private static final String IP = NetUtil.LOCALHOST4.getHostAddress();

    private final ForwardProperties forwardProperties;

    private final MyRouteDefinitionRouteLocator myRouteDefinitionRouteLocator;

    public String getLocalServiceName() {
        return forwardProperties.getBackServiceName();
    }

    /**
     * 对外查询绑定接口
     */
    public boolean isBindQueryApi(String path) {
        return path.contains(GatewayConstant.BIND_INFO_API);
    }

    /**
     * 是否转发第三方供应商服务 private-number-hmyc-third
     */
    public boolean changeThirdRoute(ServerWebExchange exchange) {
        RouteDefinition routeDefinition = RouteDefinitionCache.get(forwardProperties.getBackThirdServiceName());
        if (routeDefinition != null) {
            // 设置第三方供应商的路由
            Route route = myRouteDefinitionRouteLocator.convertToRoute(routeDefinition);

            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
            return true;
        }
        return false;
    }

    public boolean changeThirdRoute(ServerWebExchange exchange, String supplierId) {
        // 根据供应商id找具体处理服务
        Map<String, String> map = forwardProperties.getSupplierWithServiceName();
        String serviceName = map.get(supplierId);
        log.info("供应商id: {}, 请求服务: {}", supplierId, serviceName);
        if (StrUtil.isEmpty(serviceName)) {
            return false;
        }
        RouteDefinition routeDefinition = RouteDefinitionCache.get(serviceName);
        if (routeDefinition != null) {
            // 设置第三方供应商的路由
            Route route = myRouteDefinitionRouteLocator.convertToRoute(routeDefinition);

            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
            return true;
        }
        return false;
    }

    public Boolean isForward(String requestPath) {
        List<String> forwardUriList = forwardProperties.getForwardUriList();
        if (CollUtil.isEmpty(forwardUriList)) {
            return false;
        }
        for (String uri : forwardUriList) {
            if (requestPath.contains(uri)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否跳过过滤器
     */
    public Boolean passFilter(ServerHttpRequest request) {

        HttpMethod method = request.getMethod();
        if (!isPost(method)) {
            return true;
        }

        // 1. 拦截哪些uri
        String path = request.getURI().getPath();
        return !isForward(path);
    }

    public GatewayLog getGatewayLog(ServerWebExchange exchange) {
        GatewayLog gatewayLog = new GatewayLog();
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        Route route = getGatewayRoute(exchange);
        gatewayLog.setCurrentHost(IP);
        gatewayLog.setRequestMethod(request.getMethodValue());
        gatewayLog.setRequestPath(getStringAttribute(exchange, GatewayConstant.CACHED_REQUEST_PATH_KEY));
        gatewayLog.setTargetServer(route.getId());
        gatewayLog.setRequestTime(DateUtil.date());
        gatewayLog.setSourceIp(getGatewayIpAddress(request));
        gatewayLog.setBreakFlag(0);
        gatewayLog.setAreaCode(getStringAttribute(exchange, GatewayConstant.CACHED_AREA_CODE_KEY));
        gatewayLog.setBindId(getStringAttribute(exchange, GatewayConstant.CACHED_BIND_ID_KEY));
        gatewayLog.setRequestId(getStringAttribute(exchange, GatewayConstant.CACHED_REQUEST_ID_KEY));
        gatewayLog.setVccId(StrUtil.subSuf(gatewayLog.getRequestPath(), -4));
        String forwardUrl = getStringAttribute(exchange, GatewayConstant.CACHED_FORWARD_URL_KEY);
        if (StrUtil.isEmpty(forwardUrl)) {
            Optional<URI> uri = getUri(exchange);
            if (uri.isPresent()) {
                forwardUrl = uri.get().toString();
            }
        }
        gatewayLog.setForwardUrl(forwardUrl);

        String exception = getStringAttribute(exchange, GatewayConstant.CACHED_EXCEPTION_KEY);
        gatewayLog.setException(exception);

        String requestBody = getRequestBody(exchange);
        gatewayLog.setRequestBody(requestBody);

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode != null) {
            gatewayLog.setHttpStatus(statusCode.value());
        }
        return gatewayLog;
    }

    /**
     * 从请求url中获取vccId
     */
    public String getVccId(ServerWebExchange exchange) {
        String requestPath = getStringAttribute(exchange, GatewayConstant.CACHED_REQUEST_PATH_KEY);
        return StrUtil.subSuf(requestPath, -4);
    }

    public Optional<String> getSupplierId(ServerWebExchange exchange) {
        String supplierId = getStringAttribute(exchange, GatewayConstant.CACHED_SUPPLIER_ID_KEY);
        return Optional.ofNullable(supplierId);
    }

    /**
     * 设置属性
     */
    public void setAttribute(ServerWebExchange exchange, String key, String attribute) {
        if (StrUtil.isEmpty(attribute)) {
            return;
        }
        exchange.getAttributes().put(key, attribute);
    }

    /**
     * 获取表单参数
     */
    public Map<String, String> getFormParam(ServerWebExchange exchange) {

        return HttpUtil.decodeParamMap(exchange.getRequest().getURI().getQuery(), Charset.defaultCharset());
    }

    /**
     * 获取参数 string
     */
    public String getStringAttribute(ServerWebExchange exchange, String key) {

        return Convert.toStr(exchange.getAttributes().get(key), "");
    }

    public Optional<URI> getUri(ServerWebExchange exchange) {
        Object o = exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if (o != null) {
            return Optional.of((URI) o);
        }
        return Optional.empty();
    }

    public String getRequestPath(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().toString();
    }

    /**
     * 获取请求体
     */
    public String getRequestBody(ServerWebExchange exchange) {
        Object cachedRequestBodyObject = exchange.getAttributes().get(GatewayConstant.CACHED_REQUEST_BODY_OBJECT_KEY);
        return StrUtil.toString(cachedRequestBodyObject);
    }

    public String getBindId(ServerWebExchange exchange) {
        return getStringAttribute(exchange, GatewayConstant.CACHED_BIND_ID_KEY);
    }

    public String getRequestId(ServerWebExchange exchange) {
        return getStringAttribute(exchange, GatewayConstant.CACHED_REQUEST_ID_KEY);
    }

    /**
     * 从请求体 获取地市编码和接口类型
     *
     * @param exchange 请求信息
     * @param treeMap  请求体 json对象
     * @param path     请求uri
     */
    public Optional<BindTypeVO> getBindTypeVO(ServerWebExchange exchange, TreeMap<String, Object> treeMap, String path) {
        boolean isBinding = path.contains(GatewayConstant.BINDING);
        // 判断area_code是否为本机房处理,
        // 绑定接口有area_code
        String areaCode = Convert.toStr(treeMap.get(GatewayConstant.AREA_CODE));
        String bindId = Convert.toStr(treeMap.get(GatewayConstant.BIND_ID));
        String requestId = Convert.toStr(treeMap.get(GatewayConstant.REQUEST_ID));
        // 解绑 更新 根据bindId,
        if (StrUtil.isEmpty(areaCode)) {
            if (StrUtil.isEmpty(bindId)) {
                return Optional.empty();
            }
            try {
                areaCode = BindIdUtil.getAreaCodeByBindId(bindId);
                boolean isAreaCode = BindIdUtil.isAreaCode(areaCode);
                if (!isAreaCode) {
                    return Optional.empty();
                }
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        areaCode = SystemConstant.ONE.equals(Convert.toStr(treeMap.get(GatewayConstant.WHOLE_AREA))) ? SystemConstant.COUNTRY_CODE : areaCode;
        setAttribute(exchange, GatewayConstant.CACHED_BIND_ID_KEY, Convert.toStr(bindId, ""));
        setAttribute(exchange, GatewayConstant.CACHED_REQUEST_ID_KEY, Convert.toStr(requestId, ""));
        return Optional.of(BindTypeVO.builder().areaCode(areaCode).isBinding(isBinding).bindId(bindId).build());
    }

    /**
     * 解绑, 修改时, 根据bindId查询供应商id
     *
     * @param supplierWeights 供应商列表
     * @param vccId           企业id
     * @param bindId          绑定id
     * @return 供应商id
     */
    public Optional<String> getSupplierIdByBindId(List<SupplierWeight> supplierWeights, String vccId, String bindId) {
        // 获取bindId后10位 为supplierIdHash
        String supplierIdHash = BindIdUtil.getSupplierIdHash(bindId);
        Optional<SupplierWeight> first = supplierWeights.stream()
                .filter(item -> supplierIdHash.equals(Convert.toStr(BindIdUtil.getHash(item.getSupplierId()))))
                .findFirst();
        if (first.isPresent()) {
            return Optional.of(first.get().getSupplierId());
        }
        // 为空 可能是旧数据, 开关 暂定3538为hdh, 其他local, 后期运行一段时间后可以删除以下if判断
        if (forwardProperties.getSupplierSwitch()) {
            if (forwardProperties.getSupplierSwitchVccId().contains(vccId)) {
                return Optional.of(GatewayConstant.HDH);
            }
        }
        return Optional.empty();
    }

    /**
     * 获取ip
     */
    public String getGatewayIpAddress(ServerHttpRequest request) {
        String unknown = GatewayConstant.UNKNOWN;
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !unknown.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
        }
        return ip;
    }

    /**
     * 获取当前请求路由
     */
    public Route getGatewayRoute(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    }

    /**
     * 是否为post请求
     */
    public Boolean isPost(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }

    /**
     * 获取熔断器打开时的异常
     */
    public Optional<Exception> getCircuitBreakerExecutionException(ServerWebExchange exchange) {
        Object o = exchange.getAttributes().get(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        if (o != null) {
            Exception exception = (Exception) o;
            return Optional.of(exception);
        }
        return Optional.empty();
    }

    public Mono<Void> responseData(ServerWebExchange exchange, byte[] bytes) {
        // 自定义返回格式
        return Mono.defer(() -> {

            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Flux.just(buffer));
        });
    }

    public Mono<Void> responseData(ServerWebExchange exchange, Result result) {
        // 自定义返回格式
        return Mono.defer(() -> {

            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            DataBuffer buffer = response.bufferFactory().wrap(JSONObject.toJSONBytes(result));
            return response.writeWith(Flux.just(buffer));
        });
    }

    /**
     * 获取接口业务模式
     *
     * @param requestUri http请求uri
     * @return 业务模式
     */
    public Optional<String> getBusinessType(String requestUri) {
        // 对外 查询绑定绑定接口 不校验业务模式
        if (isBindQueryApi(requestUri)) {
            return Optional.empty();
        }
        try {
            List<String> list = StrUtil.split(requestUri, StrUtil.SLASH);
            if (list.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(list.get(5));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 校验签名
     *
     * @param vccId     企业id
     * @param secretKey 秘钥
     * @param paramsMap 参数
     * @return
     */
    @SuppressWarnings("all")
    public Result checkSign(String vccId, String secretKey, TreeMap paramsMap) {
        if (ObjectUtil.isEmpty(paramsMap.get("ts"))) {
            return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "ts 为空!");
        }
        String ts = String.valueOf(paramsMap.get("ts"));
        // 前后5分钟
        if (!checkTs(ts)) {
            return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "sign 已过期!");
        }

        // 验签
        String userSign = Convert.toStr(paramsMap.get("sign"));
        if (StrUtil.isEmpty(userSign)) {
            return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "sign 不存在!");

        }
        String createSign = createSign(paramsMap, secretKey);
        boolean equals = userSign.equals(createSign);
        if (!equals) {
            return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "sign 验证不通过!");

        }
        return Result.ok();
    }

    private boolean checkTs(String ts) {
        try {
            if (ts.length() == 10) {
                long between = Math.abs(System.currentTimeMillis() / 1000 - Long.parseLong(ts));
                return between <= forwardProperties.getTsTimeout();
            }
            long between = Math.abs(System.currentTimeMillis() - Long.parseLong(ts));
            return between <= forwardProperties.getTsTimeout() * 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 生成签名
     *
     * @param params    参数
     * @param secretKey 秘钥
     * @return 签名
     */
    private String createSign(Map<String, Object> params, String secretKey) {
        params.remove("appkey");
        params.remove("sign");
        params.remove("vcc_id");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE, CharUtil.TAB));
            }
        });
        paramList.add("secret_key=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }
}
