package com.cqt.sdk.client.strategy.agentstatus;

import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientChangeStatusVO;
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
 * date:  2023-07-03 13:37
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentStatusTransferStrategyFactory {

    public static final Map<String, AgentStatusTransferStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<AgentStatusTransferStrategy> agentStatusStrategyList;

    /**
     * 初始化呼叫状态策略
     */
    @PostConstruct
    public void init() {
        for (AgentStatusTransferStrategy agentStatusStrategy : agentStatusStrategyList) {
            STRATEGY_MAP.put(agentStatusStrategy.getTransferActionEnum().name(), agentStatusStrategy);
        }
    }

    /**
     * 坐席状态迁移
     *
     * @param clientChangeStatusDTO 坐席状态变更消息
     */
    public ClientChangeStatusVO transferAgentStatus(ClientChangeStatusDTO clientChangeStatusDTO) throws Exception {
        String action = clientChangeStatusDTO.getAction();
        AgentStatusTransferStrategy strategy = STRATEGY_MAP.get(action);
        if (Optional.ofNullable(strategy).isPresent()) {
            return strategy.deal(clientChangeStatusDTO);
        }
        return ClientChangeStatusVO.response(clientChangeStatusDTO, "-1", "action非法!", true);
    }
}
