package com.cqt.client.service;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.cqt.base.initialize.ApplicationInfoInitialize;
import com.cqt.client.config.nacos.CloudNettyProperties;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.rpc.client.ClientServerRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import static com.cqt.client.websocket.WebSocketHandler.channelMap;

/**
 * @author linshiqiang
 * date:  2023-06-29 10:44
 */
@Slf4j
@DubboService
@Service
@RequiredArgsConstructor
public class ClientServerRemoteServiceImpl implements ClientServerRemoteService {

    private final RedissonUtil redissonUtil;

    private final CloudNettyProperties cloudNettyProperties;

    private final ObjectMapper objectMapper;

    @Override
    public ClientResponseBaseVO request(String requestBody) {
        log.info("{}", requestBody);
        JSONObject jsonObject;
        try {
            jsonObject = objectMapper.readValue(requestBody, JSONObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String agentId = jsonObject.getString("agent_id");
        String companyCode = jsonObject.getString("company_code");
        String os = jsonObject.getString("os");
        String channelJson = redissonUtil.getString("netty:client:" + companyCode + "_" + agentId + "_" + os);
        if (StringUtils.isBlank(channelJson)) {
            log.error("未找到坐席{}对应的redis信息", companyCode + "_" + agentId + "_" + os);
            return ClientResponseBaseVO.fail("1", "未找到坐席对应的redis信息");
        }
        JSONObject channelObj = JSONObject.parseObject(channelJson);
        String channelId = channelObj.getString("key");
        log.info("redis查询数据{}，通道Id：{}，坐席Id：{}", jsonObject, channelId, agentId);
        if (StringUtils.isBlank(channelId)) {
            log.error("未找到坐席{}对应的通道", companyCode + "_" + agentId);
            return ClientResponseBaseVO.fail("1", "未找到坐席对应的通道");
        }
        log.info("转发接口调用，通道Id：{},消息体：{}", channelId, JSONObject.toJSONString(jsonObject));
        ClientResponseBaseVO clientResponseBaseVO;
        try {
            if (channelMap.containsKey(channelId)) {
                Channel channel = channelMap.get(channelId);
                if (channel == null) {
                    log.error(channelId + "通道丢失或异常");
                    return ClientResponseBaseVO.fail("1", "通道丢失或异常");
                }
                channel.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(jsonObject)));
                log.info("客户端转发成功，通道Id：{},坐席Id：{}", channelId, companyCode + "_" + agentId);
                return ClientResponseBaseVO.response(objectMapper.readValue(requestBody, ClientRequestBaseDTO.class), "0", "转发成功");
            } else {
                String serverIp = channelObj.getString("server_ip");
                String hostAddress = ApplicationInfoInitialize.SERVER_IP;
                log.info("当前服务器IP: {}, 通道ip: {}", hostAddress, serverIp);
                if (hostAddress.equals(serverIp)) {
                    log.info("通道丢失异常，消息返回失败");
                    return ClientResponseBaseVO.fail("1", "通道丢失异常，消息返回失败");
                }
                log.info("通道不在本台服务器，尝试转发消息，通道Id：{}", channelId);
                String url = StrFormatter.format(cloudNettyProperties.getSyncChannelUrl(), serverIp);
                try {
                    String s = HttpUtil.post(url, requestBody, 100000);
                    clientResponseBaseVO = objectMapper.readValue(s, ClientResponseBaseVO.class);
                } catch (Exception e) {
                    log.error("转发到" + serverIp + "服务器发生异常", e);
                    return ClientResponseBaseVO.fail("0", "转发到" + serverIp + "服务器发生异常");
                }
            }
        } catch (Exception e) {
            log.error("发送消息失败，通道Id：{}，异常原因：{}", channelId, e.getMessage());
            return ClientResponseBaseVO.fail("0", "发送消息失败");
        }
        log.info("响应结果：{}", clientResponseBaseVO);
        return clientResponseBaseVO;
    }

}
