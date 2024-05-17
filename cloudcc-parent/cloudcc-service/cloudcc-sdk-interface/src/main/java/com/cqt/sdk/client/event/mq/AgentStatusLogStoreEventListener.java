package com.cqt.sdk.client.event.mq;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import com.cqt.sdk.client.service.DataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-05 16:01
 * 坐席状态迁移日志事件 发送mq进行入库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentStatusLogStoreEventListener implements ApplicationListener<AgentStatusLogStoreEvent> {

    private final RocketMQTemplate rocketMQTemplate;

    private final DataQueryService dataQueryService;

    @Override
    public void onApplicationEvent(AgentStatusLogStoreEvent event) {
        AgentStatusTransferDTO dto = event.getAgentStatusTransferDTO();
        if (!dataQueryService.isDealAgentStatus(dto.getTargetStatus(), dto.getSourceStatus())) {
            return;
        }
        // 发mq进行入库
        Message<AgentStatusTransferDTO> message = MessageBuilder.withPayload(dto)
                .setHeader(RocketMQHeaders.KEYS, dto.getAgentId())
                .build();
        String destination = RocketMqConstant.AGENT_STATUS_LOG_TOPIC + StrUtil.COLON + dto.getCompanyCode();
        SendResult sendResult = rocketMQTemplate.syncSend(destination, message);
        log.debug("[坐席状态日志事件] 发送mq结果: {}", sendResult.getSendStatus());
    }

}
