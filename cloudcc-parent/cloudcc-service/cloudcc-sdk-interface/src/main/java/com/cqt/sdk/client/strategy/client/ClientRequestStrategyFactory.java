package com.cqt.sdk.client.strategy.client;

import com.cqt.cloudcc.manager.util.ExceptionUtil;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
public class ClientRequestStrategyFactory {

    public static final Map<String, ClientRequestStrategy> CLIENT_REQUEST_STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<ClientRequestStrategy> callStatusStrategyList;

    private final ObjectMapper objectMapper;

    /**
     * 初始化呼叫状态策略
     */
    @PostConstruct
    public void init() {
        for (ClientRequestStrategy clientRequestStrategy : callStatusStrategyList) {
            CLIENT_REQUEST_STRATEGY_MAP.put(clientRequestStrategy.getMsgType().name(), clientRequestStrategy);
        }
    }

    /**
     * @param requestBody 请求参数内容
     */
    public ClientBase dealCallStatus(ClientRequestBaseDTO requestBaseDTO, String requestBody) {
        try {
            String msgType = requestBaseDTO.getMsgType();
            log.info("[客户端SDK], 消息类型: {}, 消息: {}", msgType, requestBody);
            ClientRequestStrategy strategy = CLIENT_REQUEST_STRATEGY_MAP.get(msgType);
            if (Optional.ofNullable(strategy).isPresent()) {
                ClientBase clientBase = strategy.deal(requestBody);
                log.info("[客户端SDK], 消息类型: {}, 处理结果: {}", msgType, objectMapper.writeValueAsString(clientBase));
                // 通知客户端SDK, 在netty侧异步收到请求根据reply字段决定是否通知ws客户端
                return clientBase;
            }
            return ClientResponseBaseVO.response(requestBaseDTO, "1", "msg_type非法!");
        } catch (Exception e) {
            log.error("[客户端SDK] 请求参数: {}, 请求异常: ", requestBody, e);
            return ExceptionUtil.exceptionControl(e);
        }
    }

}
