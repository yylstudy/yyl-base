package com.cqt.sdk.client.manager;

import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-05 17:58
 * 坐席权值信息 广播
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = "cloudcc_broadcast_topic",
        consumerGroup = "agentWeightGroup",
        selectorExpression = "agentWeight",
        messageModel = MessageModel.BROADCASTING,
        consumeThreadNumber = 1)
public class AgentWeightBroadcastMessageConsumer implements RocketMQListener<AgentCheckinCacheDTO> {

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public void onMessage(AgentCheckinCacheDTO agentCheckinCacheDTO) {
        commonDataOperateService.dealAgentCheckinCache(agentCheckinCacheDTO);
    }

}
