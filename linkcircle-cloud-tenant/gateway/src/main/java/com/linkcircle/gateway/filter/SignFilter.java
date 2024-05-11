//package com.linkcircle.gateway.filter;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.linkcircle.gateway.common.GlobalConstants;
//import com.linkcircle.gateway.common.HmacSHA256Signature;
//import com.linkcircle.gateway.config.SignIncludePath;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferUtils;
//import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.util.PathMatcher;
//import org.springframework.util.StringUtils;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.TreeMap;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2023/4/20 15:32
// */
//@Component
//@Slf4j
//public class SignFilter implements GlobalFilter, Ordered {
//    private PathMatcher pathMatcher = new AntPathMatcher();
//    private static ObjectMapper mapper = new ObjectMapper();
//    @Autowired
//    private ReactiveStringRedisTemplate redisTemplate;
//
//    @Autowired
//    private SignIncludePath signIncludePath;
//
//    @Value("${sign.expireSecond}")
//    private long expireSecond;
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        ServerHttpResponse resp = exchange.getResponse();
//        ServerHttpRequest request = exchange.getRequest();
//        String url = request.getURI().getPath();
//        boolean isSignPath = signIncludePath.getAllPathList().stream()
//                .anyMatch(includePath->pathMatcher.match(includePath,url));
//        if(!isSignPath){
//            return chain.filter(exchange);
//        }
//        String timestampStr = request.getHeaders().getFirst(GlobalConstants.X_TIMESTAMP);
//        if(!StringUtils.hasText(timestampStr)){
//            return getVoidMono(resp, HttpStatus.OK.value(),"时间戳不能为空");
//        }
//        String headerSign = request.getHeaders().getFirst(GlobalConstants.X_SIGN);
//        if(!StringUtils.hasText(headerSign)){
//            return getVoidMono(resp, HttpStatus.OK.value(),"签名不能为空");
//        }
//        long timestamp;
//        try{
//            timestamp = Long.parseLong(timestampStr);
//        }catch (Exception e){
//            return getVoidMono(resp, HttpStatus.OK.value(),"时间戳格式不正确");
//        }
//        long currentTimeStamp = System.currentTimeMillis();
//        if(currentTimeStamp>(timestamp+expireSecond*1000)){
//            return getVoidMono(resp, HttpStatus.OK.value(),"接口时间戳已过期");
//        }
//        String appKey = request.getHeaders().getFirst(GlobalConstants.APP_KEY);
//        if(!StringUtils.hasText(appKey)){
//            return getVoidMono(resp, HttpStatus.OK.value(),"appKey不能为空");
//        }
//        String redisAppkey = GlobalConstants.APP_PREFIX+appKey;
//        Mono<String> appMono = redisTemplate.opsForValue().get(redisAppkey);
//        Mono<Void> mono = appMono.flatMap(appSecret->{
//            Map<String, String> paramMap = new TreeMap<>();
//            paramMap.put(GlobalConstants.X_TIMESTAMP,timestampStr);
//            paramMap.put(GlobalConstants.APP_KEY,appKey);
//            request.getQueryParams().forEach((key,value)->{
//                if(value!=null){
//                    paramMap.put(key,value.get(0));
//                }
//            });
//            paramMap.remove("_t");
//            if(request.getMethod()== HttpMethod.GET){
//                String sign = sign(paramMap,appSecret);
//                if(!Objects.equals(sign,headerSign)){
//                    return getVoidMono(resp, HttpStatus.OK.value(),"签名校验错误");
//                }
//                return chain.filter(exchange);
//            }
//            return DataBufferUtils.join(request.getBody()).flatMap(dataBuffer -> {
//                byte[] bytes = new byte[dataBuffer.readableByteCount()];
//                dataBuffer.read(bytes);
//                String bodyString = new String(bytes, StandardCharsets.UTF_8);
//                Map bodyMap = JSONObject.parseObject(bodyString,Map.class);
//                if(bodyMap!=null){
//                    paramMap.putAll(bodyMap);
//                }
//                String sign = sign(paramMap,appSecret);
//                log.debug("paramMap:{}",paramMap);
//                if(!Objects.equals(sign,headerSign)){
//                    return getVoidMono(resp, HttpStatus.OK.value(),"签名校验错误");
//                }
//                DataBufferUtils.release(dataBuffer);
//                Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
//                    DataBuffer buffer = exchange.getResponse().bufferFactory()
//                            .wrap(bytes);
//                    return Mono.just(buffer);
//                });
//                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
//                        exchange.getRequest()) {
//                    @Override
//                    public Flux<DataBuffer> getBody() {
//                        return cachedFlux;
//                    }
//                };
//                return chain.filter(exchange.mutate().request(mutatedRequest).build());
//            });
//        }).switchIfEmpty(getVoidMono(resp, HttpStatus.OK.value(),"无效的appKey"));
//        return mono;
//    }
//
//    public static String sign(Map<String, String> paramMap,String appSecret){
//        String jsonStr = JSONObject.toJSONString(paramMap, SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteNullStringAsEmpty);
//        String sign = HmacSHA256Signature.create().computeSignature(appSecret,jsonStr);
//        return sign;
//    }
//
//    private Mono<Void> getVoidMono(ServerHttpResponse serverHttpResponse, int errorCode, String errorMsg) {
//        try{
//            serverHttpResponse.getHeaders().add("Character-Encoding", "UTF-8");
//            serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
//            serverHttpResponse.setStatusCode(HttpStatus.valueOf(errorCode));
//            Map<String,Object> result = new HashMap<>();
//            result.put("success",false);
//            result.put("code",errorCode);
//            result.put("message",errorMsg);
//            result.put("timestamp",System.currentTimeMillis());
//            DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(mapper.writeValueAsString(result).getBytes());
//            return serverHttpResponse.writeWith(Flux.just(dataBuffer));
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public int getOrder() {
//        return -1;
//    }
//
//
//}
