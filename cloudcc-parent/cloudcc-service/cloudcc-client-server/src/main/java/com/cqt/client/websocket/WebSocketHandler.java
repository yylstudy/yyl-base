package com.cqt.client.websocket;

import com.alibaba.fastjson.JSONObject;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.util.RpcContextUtil;
import com.cqt.client.config.nacos.CloudNettyProperties;
import com.cqt.client.service.TokenVerifyService;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.vo.ClientCheckinVO;
import com.cqt.rpc.call.CallControlRemoteService;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.cqt.client.websocket.ChannelAuthHandler.loginMap;

/**
 * @Author chw
 * @Date 20230615
 * @Description websocket处理器
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final String HEART_LOGO = "ping";

    private static final String CHECK_LOGO = "checkin";

    private static final String SUCCESS_LOGO = "0";

    private static final String NULL = "null";

    private static final String CONNECTOR = "_";

    /**
     * 通道map，存储channel，用于群发消息，以及统计客户端的在线数量
     */
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * 任务map，存储future，用于重连检测
     */
    public static Map<String, Future<?>> futureMap = new ConcurrentHashMap<>();

    /**
     * 存储channel的id和用户主键的映射，已经签入的坐席
     */
    public static Map<String, String> clientMap = new ConcurrentHashMap<>();

    public static Map<String, String> extMap = new ConcurrentHashMap<>();

    private final CloudNettyProperties cloudNettyProperties;

    private final RedissonUtil redissonUtil;

    private final TokenVerifyService tokenVerifyService;

    @DubboReference
    private CallControlRemoteService callControlRemoteService;

    @DubboReference
    private SdkInterfaceRemoteService sdkInterfaceRemoteService;

    private final ObjectMapper objectMapper;

    @Resource(name = "otherRoomExecutor")
    private ThreadPoolTaskExecutor otherExecutor;

    /**
     * 客户端发送给服务端的消息
     *
     * @Author chw
     * @Date 20230615
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            // 接受客户端发送的消息
            ClientRequestBaseDTO dto = objectMapper.readValue(msg.text(), ClientRequestBaseDTO.class);
            String agentId = dto.getAgentId();
            String msgType = dto.getMsgType();
            String companyCode = dto.getCompanyCode();
            String os = dto.getOs();
            // 每个channel都有id，asLongText是全局channel唯一id
            String key = ctx.channel().id().asLongText();
            log.info("坐席: {}, 发起请求: {}, 消息: {} ", agentId, msgType, msg.text());
            if (!tokenVerifyService.checkToken(dto.getToken(), msgType)) {
                ClientResponseBaseVO fail = ClientResponseBaseVO.fail(SdkErrCode.TOKEN_VERIFY_FAIL);
                ctx.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(fail)));
                ctx.close();
                return;
            }
            if (HEART_LOGO.equals(msgType)) {
                this.heartReply(ctx, dto);
                return;
            }
            if (CHECK_LOGO.equals(msgType)) {
                checkInDo(ctx, msg, agentId, companyCode, os);
                return;
            }
            if (!(companyCode + CONNECTOR + agentId + CONNECTOR + os).equals(clientMap.get(key)) && !Objects.equals(msgType, "get_status")) {
                ClientResponseBaseVO fail = ClientResponseBaseVO.fail(SdkErrCode.AGENT_NOT_CHECKIN);
                ctx.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(fail)));
                ctx.close();
                return;
            }
            if (cloudNettyProperties.getCallType().contains(msgType)) {
                try {
                    setRpcTraceId(dto);
                    replyToSdk(ctx, agentId, msgType, companyCode, os, callControlRemoteService.request(msg.text()), dto);
                } catch (Exception e) {
                    log.error("msg: {}, 调用CallController失败，error: ", msg.text(), e);
                }
            } else if (cloudNettyProperties.getSdkType().contains(msgType)) {
                try {
                    setRpcTraceId(dto);
                    replyToSdk(ctx, agentId, msgType, companyCode, os, sdkInterfaceRemoteService.request(msg.text()), dto);
                } catch (Exception e) {
                    ClientResponseBaseVO re = ClientResponseBaseVO.response(dto, "1", "请求失败", false);
                    ctx.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(re)));
                    log.error("msg: {}, 调用SDKInterface失败，error：", msg.text(), e);
                }
            }
        } catch (Exception e) {
            log.error("msg: {}, websocket服务器推送消息发生错误：", msg.text(), e);
        }
    }

    private void setRpcTraceId(ClientRequestBaseDTO dto) {
        RpcContextUtil.set(dto.getReqId(), dto.getMsgType(), dto.getCompanyCode(), dto.getAgentId(), dto.getExtId());
    }

    private void checkInDo(ChannelHandlerContext ctx, TextWebSocketFrame msg, String agentId, String companyCode, String os) {
        String rescode = null;
        ClientCheckinVO clientCheckinVO = null;
        try {
            ClientBase clientBase = sdkInterfaceRemoteService.request(msg.text());
            clientCheckinVO = (ClientCheckinVO) clientBase;
            rescode = clientCheckinVO.getCode();
            ctx.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(clientCheckinVO)));
            log.info("坐席：" + companyCode + CONNECTOR + agentId + CONNECTOR + os + "回复了 checkin 的消息，消息内容：{}", objectMapper.writeValueAsString(clientCheckinVO));
        } catch (Exception e) {
            log.error("登录请求失败，坐席Id：{}_{},客户端断开", companyCode, agentId);
        }
        if (SUCCESS_LOGO.equals(rescode)) {
            clientMap.put(ctx.channel().id().asLongText(), companyCode + CONNECTOR + agentId + CONNECTOR + os);
            extMap.put(companyCode + CONNECTOR + agentId + CONNECTOR + os, clientCheckinVO.getExtId());
            log.info("坐席：" + companyCode + CONNECTOR + agentId + CONNECTOR + os + "签入成功");
        }
    }

    private void heartReply(ChannelHandlerContext ctx, ClientRequestBaseDTO dto) throws JsonProcessingException {
        ClientResponseBaseVO clientResponseBaseVO = new ClientResponseBaseVO();
        clientResponseBaseVO.setMsgType("pong");
        clientResponseBaseVO.setCode(SUCCESS_LOGO);
        clientResponseBaseVO.setMsg("心跳成功");
        clientResponseBaseVO.setReqId(dto.getReqId());
        ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(clientResponseBaseVO)));
        log.info(clientResponseBaseVO + "通道Id：" + ctx.channel().id().asLongText() + "坐席Id：" + loginMap.get(ctx.channel().id().asLongText()));
    }

    private void replyToSdk(ChannelHandlerContext ctx, String agentId, String msgType, String companyCode, String os, ClientBase clientBase, ClientRequestBaseDTO dto) {
        try {
            String jsonResult = objectMapper.writeValueAsString(clientBase);
            if (StringUtils.isNotBlank(jsonResult) && !NULL.equals(jsonResult)) {
                ctx.writeAndFlush(new TextWebSocketFrame(jsonResult));
                log.info("坐席：" + companyCode + CONNECTOR + agentId + CONNECTOR + os + "回复了" + msgType + "的消息，消息内容：{}", jsonResult);
            } else {
                ClientResponseBaseVO result = ClientResponseBaseVO.response(dto, "1", "请求失败", false);
                ctx.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(result)));
                log.error("坐席：" + companyCode + CONNECTOR + agentId + CONNECTOR + os + "回调信息为空");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 客户端连接时候的操作
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("一个客户端连接......" + ctx.channel().remoteAddress() + "," + Thread.currentThread().getName());
        String key = ctx.channel().id().asLongText();
        if (!channelMap.containsKey(key)) {
            channelMap.put(key, ctx.channel());
        } else {
            // 每次客户端和服务的主动通信，和服务端周期向客户端推送消息互不影响 解决问题一
            ctx.channel().writeAndFlush(new TextWebSocketFrame(Thread.currentThread().getName() + "服务器时间" + LocalDateTime.now() + "wdy"));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ClientResponseBaseVO clientResponseBaseVO = new ClientResponseBaseVO();
        clientResponseBaseVO.setMsgType("connect");
        clientResponseBaseVO.setCode(SUCCESS_LOGO);
        clientResponseBaseVO.setMsg("连接成功");
        otherExecutor.execute(() -> {
            try {
                Thread.sleep(200);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(clientResponseBaseVO)));
            } catch (JsonProcessingException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 客户端掉线时的操作
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (loginMap.containsKey(ctx.channel().id().asLongText())) {
            offlineMethod(ctx, cloudNettyProperties.getReconnectionTime());
        }
    }

    private void offlineMethod(ChannelHandlerContext ctx, Integer i) {
        ClientRequestBaseDTO clientRequestBaseDTO = new ClientRequestBaseDTO();
        String key = ctx.channel().id().asLongText();
        String s = clientMap.get(key);
        if (StringUtils.isNotBlank(s)) {
            String[] agentInfo = s.split(CONNECTOR);
            clientRequestBaseDTO.setCompanyCode(agentInfo[0]);
            clientRequestBaseDTO.setAgentId(agentInfo[1] + CONNECTOR + agentInfo[2]);
            clientRequestBaseDTO.setOs(agentInfo[3]);
            clientRequestBaseDTO.setMsgType("checkout");
            clientRequestBaseDTO.setReqId("netty-auto-checkout");
        }
        String checkinAgentInfo = loginMap.get(key);
        if (StringUtils.isNotBlank(checkinAgentInfo)) {
            String s1 = redissonUtil.getString("netty:client:" + checkinAgentInfo);
            if (StringUtils.isNotBlank(s1)) {
                JSONObject jsonObject1 = JSONObject.parseObject(s1);
                if (key.equals(jsonObject1.getString("key"))) {
                    redissonUtil.delKey("netty:client:" + checkinAgentInfo);
                }
            }
        }
        Future<?> scheduledFuture = ctx.channel().eventLoop().scheduleAtFixedRate(new WebsocketRunnable(ctx, clientRequestBaseDTO, redissonUtil, sdkInterfaceRemoteService, objectMapper), i, cloudNettyProperties.getReconnectionTime(), TimeUnit.SECONDS);
        futureMap.put(key, scheduledFuture);
        // 移除通信过的channel
        channelMap.remove(key);
        // 移除和用户绑定的channel
        clientMap.remove(key);
        loginMap.remove(key);
        log.info("一个客户端移除......" + ctx.channel().remoteAddress() + "坐席{}断开连接", checkinAgentInfo);
    }

    /**
     * 发生异常时执行的操作
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常发生: ", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state().equals(IdleState.READER_IDLE)) {
                String id = ctx.channel().id().asLongText();
                log.info("坐席：" + loginMap.get(id) + "客户端" + cloudNettyProperties.getReadOutTime() + "s内无响应");
                offlineMethod(ctx, 2);
                ctx.close();
                log.info("坐席：" + loginMap.get(id) + "连接断开");
            }

        }
        super.userEventTriggered(ctx, evt);
    }
}
