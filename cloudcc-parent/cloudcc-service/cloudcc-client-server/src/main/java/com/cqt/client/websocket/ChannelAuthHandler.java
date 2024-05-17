package com.cqt.client.websocket;

import com.alibaba.fastjson.JSONObject;
import com.cqt.base.initialize.ApplicationInfoInitialize;
import com.cqt.client.config.nacos.CloudNettyProperties;
import com.cqt.starter.redis.util.RedissonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 86180
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class ChannelAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    /**
     * 登录连接后的坐席信息
     */
    public static Map<String, String> loginMap = new ConcurrentHashMap<>();

    private final CloudNettyProperties cloudNettyProperties;

    private final RedissonUtil redissonUtil;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        String[] auths = uri.split("/");
        String base64 = auths[2];
        byte[] decode = Base64.getUrlDecoder().decode(base64);
        String s = new String(decode);
        JSONObject authJson = JSONObject.parseObject(s);
        String agentId = authJson.getString("agent_id");
        String companyCode = authJson.getString("company_code");
        String os = authJson.getString("os");
        if (StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(companyCode) && StringUtils.isNotBlank(os)) {
            log.info(companyCode + "_" + agentId + "_" + os + "token校验通过");
            String url = cloudNettyProperties.getUrl();
            request.setUri(url);
            // 传递到下一个handler：升级握手
            JSONObject result = new JSONObject();
            result.put("server_ip", ApplicationInfoInitialize.SERVER_IP);
            result.put("key", ctx.channel().id().asLongText());
            redissonUtil.setString("netty:client:" + companyCode + "_" + agentId + "_" + os,  JSONObject.toJSONString(result),3600 * 24L, TimeUnit.SECONDS);
            log.info("存redis key ：{} value ：{}", "netty:client:" + companyCode + "_" + agentId + "_" + os, JSONObject.toJSONString(result));
            String key = ctx.channel().id().asLongText();
            loginMap.put(key, companyCode + "_" + agentId + "_" + os);
            ctx.fireChannelRead(request.retain());
            // 在本channel上移除这个handler消息处理，即只处理一次，鉴权通过与否
            ctx.pipeline().remove(ChannelAuthHandler.class);
        } else {
            ctx.close();
        }
    }

}
