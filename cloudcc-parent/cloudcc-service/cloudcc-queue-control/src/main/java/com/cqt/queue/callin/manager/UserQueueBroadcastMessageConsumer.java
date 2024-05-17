package com.cqt.queue.callin.manager;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.queue.callin.cache.UserQueueCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-05 17:58
 * 用户排队信息同步
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = "cloudcc_broadcast_topic",
        consumerGroup = "userQueueGroup",
        selectorExpression = "userQueue",
        messageModel = MessageModel.BROADCASTING,
        consumeThreadNumber = 1)
public class UserQueueBroadcastMessageConsumer implements RocketMQListener<UserQueueSyncDTO> {

    @Override
    public void onMessage(UserQueueSyncDTO userQueueSyncDTO) {
        UserQueueUpDTO userQueueUpDTO = userQueueSyncDTO.getUserQueueUpDTO();
        if (OperateTypeEnum.INSERT.equals(userQueueSyncDTO.getOperateTypeEnum())) {
            UserQueueCache.put(userQueueUpDTO.getCompanyCode(), userQueueUpDTO);
            log.info("[排队sync-新增] 企业: {}, uuid: {}", userQueueUpDTO.getCompanyCode(), userQueueUpDTO.getUuid());
            return;
        }
        UserQueueCache.remove(userQueueSyncDTO.getCompanyCode(), userQueueSyncDTO.getUuid());
        log.info("[排队sync-移除] 企业: {}, uuid: {}", userQueueSyncDTO.getCompanyCode(), userQueueSyncDTO.getUuid());
    }

}
