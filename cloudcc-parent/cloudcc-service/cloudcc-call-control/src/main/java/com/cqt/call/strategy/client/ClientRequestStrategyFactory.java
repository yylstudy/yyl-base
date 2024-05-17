package com.cqt.call.strategy.client;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.cloudcc.manager.util.ExceptionUtil;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientRequestStrategyFactory implements CommandLineRunner {

    public static final Map<String, ClientRequestStrategy> CLIENT_REQUEST_STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<ClientRequestStrategy> callStatusStrategyList;

    private final ObjectMapper objectMapper;

    /**
     * 初始化呼叫状态策略
     */
    @Override
    public void run(String... args) {
        for (ClientRequestStrategy clientRequestStrategy : callStatusStrategyList) {
            CLIENT_REQUEST_STRATEGY_MAP.put(clientRequestStrategy.getMsgType().name(), clientRequestStrategy);
        }
        log.info("初始化客户端SDK请求策略: {}", CLIENT_REQUEST_STRATEGY_MAP.size());
    }

    /**
     * @param requestBody 请求参数内容
     */
    public ClientBase dealClientRequest(ClientRequestBaseDTO requestBaseDTO, String requestBody) {
        try {
            String msgType = requestBaseDTO.getMsgType();
            if (StrUtil.isEmpty(msgType)) {
                return ClientResponseBaseVO.fail(SdkErrCode.PARAM_ERROR.getCode(), "[msg_type]不能为空");
            }
            log.info("客户端SDK, 消息类型: {}, 消息: {}", msgType, requestBody);
            ClientRequestStrategy strategy = CLIENT_REQUEST_STRATEGY_MAP.get(msgType);
            if (Optional.ofNullable(strategy).isPresent()) {
                ClientResponseBaseVO responseBaseVO = strategy.deal(requestBody);
                log.info("客户端SDK, 消息类型: {}, 处理结果: {}", msgType, objectMapper.writeValueAsString(responseBaseVO));
                return responseBaseVO;
            }
            log.error("[客户端SDK] msg_type非法: {}", requestBody);
            return ClientResponseBaseVO.response(requestBaseDTO, SdkErrCode.MSG_TYPE_INVALID);
        } catch (Exception e) {
            log.error("客户端请求参数: {}, 请求异常: ", requestBody, e);
            return ExceptionUtil.exceptionControl(e);
        }
    }

}
