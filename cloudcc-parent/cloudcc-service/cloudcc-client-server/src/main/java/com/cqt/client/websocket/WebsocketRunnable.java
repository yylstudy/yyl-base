package com.cqt.client.websocket;

import com.alibaba.fastjson.JSONObject;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;
import java.util.concurrent.Future;

import static com.cqt.client.websocket.WebSocketHandler.*;

/**
 * @author 86180
 */
@Slf4j
@Data
public class WebsocketRunnable implements Runnable {
    private  ChannelHandlerContext ctx;
    private  ClientRequestBaseDTO clientRequestBaseDTO;

    private RedissonUtil redissonUtil;
    private SdkInterfaceRemoteService sdkInterfaceRemoteService;
    private ObjectMapper objectMapper;
    private final static String CONNECTOR = "_";

    public WebsocketRunnable(ChannelHandlerContext ctx, ClientRequestBaseDTO clientRequestBaseDTO, RedissonUtil redissonUtil, SdkInterfaceRemoteService service, ObjectMapper objectMapper) {
        this.ctx = ctx;
        this.clientRequestBaseDTO = clientRequestBaseDTO;
        this.sdkInterfaceRemoteService = service;
        this.redissonUtil = redissonUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        Future<?> future = futureMap.get(ctx.channel().id().asLongText());
        if (future == null) {
            Thread.currentThread().interrupt();
            return;
        }
        log.info("断线发生30s");
        log.info(clientRequestBaseDTO + "通道Id：" + ctx.channel().id().asLongText() + "坐席Id：" + clientRequestBaseDTO.getAgentId());
        String agentId = clientRequestBaseDTO.getAgentId();
        String vccId = clientRequestBaseDTO.getCompanyCode();
        String os = clientRequestBaseDTO.getOs();
        if (StringUtils.isEmpty(agentId) || StringUtils.isEmpty(os) || StringUtils.isEmpty(vccId)) {
            log.info("坐席未签入，不做签出操作");
            future.cancel(false);
            futureMap.remove(ctx.channel().id().asLongText());
            return;
        }
        String s = redissonUtil.getString("netty:client:" + vccId + "_" + agentId + "_" + os);
        if (!StringUtils.isBlank(s)) {
            log.info("期间"+ vccId + "_" + agentId + "_" + os +"重连成功，不签出坐席");
            future.cancel(false);
            futureMap.remove(ctx.channel().id().asLongText());
            return;
        }
        String extId = extMap.get(vccId + CONNECTOR + agentId + CONNECTOR + os);
        log.info("坐席{}，绑定的分机为：{}", vccId + "_" + agentId + "_" + os, extId);
        if (StringUtils.isBlank(extId)) {
            extId = agentId;
        }
        clientRequestBaseDTO.setExtId(extId);
        ExtStatusDTO extStatusDTO = null;
        String extStatusKey = CacheUtil.getExtStatusKey(vccId, extId);
        try {
            extStatusDTO = redissonUtil.get(extStatusKey, ExtStatusDTO.class);
        } catch (Exception e) {
            log.error("坐席号：" + agentId + "获取分机状态失败", e);
        }
        if (extStatusDTO == null) {
            log.error("坐席号：" + agentId + "获取分机状态失败");
        } else {
            String targetStatus = extStatusDTO.getTargetStatus();
            log.info("分机状态:{}", targetStatus);
        }
        if (extStatusDTO == null || StringUtils.isBlank(extStatusDTO.getTargetStatus()) || ExtStatusEnum.ONLINE.toString().equals(extStatusDTO.getTargetStatus()) || ExtStatusEnum.OFFLINE.toString().equals(extStatusDTO.getTargetStatus())) {
            try {
                sdkInterfaceRemoteService.request(objectMapper.writeValueAsString(clientRequestBaseDTO));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.info("超过重连时间，自动签出坐席：{}", vccId + "_" + agentId + "_" + os);
            future.cancel(false);
            futureMap.remove(ctx.channel().id().asLongText());
            log.info("坐席"+ vccId + "_" + agentId + "_" + os +"停止定时任务");
        } else {
            log.info("坐席"+ vccId + "_" + agentId + "_" + os +"分机处于通话态，不进行签出等待下次检测");
        }
    }
}
