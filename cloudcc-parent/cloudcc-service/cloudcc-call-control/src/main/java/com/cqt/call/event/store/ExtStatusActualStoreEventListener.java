package com.cqt.call.event.store;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.call.converter.ModelConverter;
import com.cqt.call.service.DataStoreService;
import com.cqt.cloudcc.manager.config.CommonThreadPoolConfig;
import com.cqt.model.cdr.entity.ExtStatusLog;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author linshiqiang
 * date:  2023-07-05 16:01
 * 坐席状态迁移日志事件 发送mq进行入库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExtStatusActualStoreEventListener implements ApplicationListener<ExtStatusActualStoreEvent> {

    private final RocketMQTemplate rocketMQTemplate;

    private final ObjectMapper objectMapper;

    private final DataStoreService dataStoreService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @SneakyThrows
    @Override
    public void onApplicationEvent(@Nonnull ExtStatusActualStoreEvent event) {
        ExtStatusLog extStatusLog = event.getExtStatusLog();
        if (log.isDebugEnabled()) {
            log.debug("[分机状态日志事件] 收到消息: {}", objectMapper.writeValueAsString(extStatusLog));
        }

        // 保存分机实时状态到redis
        ExtStatusDTO extStatusDTO = ModelConverter.INSTANCE.extStatusLog2ExtStatus(extStatusLog);
        dataStoreService.updateActualExtStatus(extStatusDTO);

        if (!cloudCallCenterProperties.getExtStatusLog()) {
            return;
        }
        try {
            // 发mq进行入库
            Message<ExtStatusLog> message = MessageBuilder.withPayload(extStatusLog)
                    .setHeader(MessageConst.PROPERTY_KEYS, extStatusLog.getExtId())
                    .build();
            String destination = RocketMqConstant.CLOUD_CC_EXT_LOG_TOPIC + StrUtil.COLON + extStatusLog.getCompanyCode();
            SendResult sendResult = rocketMQTemplate.syncSend(destination, message);
            log.debug("[分机状态日志事件] 发送mq结果: {}", sendResult);
        } catch (Exception e) {
            // TODO 发送失败
            log.error("[分机状态日志事件] data: {}, 发送mq异常: ", extStatusLog, e);
        }

    }
}
