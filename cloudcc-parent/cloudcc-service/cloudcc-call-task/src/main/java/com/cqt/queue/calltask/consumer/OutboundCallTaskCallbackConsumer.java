package com.cqt.queue.calltask.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.model.calltask.entity.OutboundCallTaskCdr;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.cqt.queue.calltask.mapper.OutboundCallTaskCdrMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:59
 * 外呼任务结果回调
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = RocketMqConstant.CLOUD_CC_OUTBOUND_CALL_TASK,
        consumerGroup = RocketMqConstant.APP_NAME,
        selectorExpression = RocketMqConstant.OUTBOUND_CALL_TASK_DB_TAG)
public class OutboundCallTaskCallbackConsumer implements RocketMQListener<OutboundCallTaskCdr> {

    private final OutboundCallTaskCdrMapper outboundCallTaskCdrMapper;

    private final Snowflake snowflake;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(OutboundCallTaskCdr outboundCallTaskCdr) {
        Date callStartTime = outboundCallTaskCdr.getCallStartTime();
        String month = DateUtil.format(callStartTime, CommonConstant.MONTH_FORMAT);
        RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, outboundCallTaskCdr.getCompanyCode());
        RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
        try {
            outboundCallTaskCdr.setCallId(snowflake.nextId());
            outboundCallTaskCdrMapper.insert(outboundCallTaskCdr);
        } catch (Exception e) {
            log.error("[MQ事件] 消息: {}, 消费异常: {}", outboundCallTaskCdr, e);
            if (e instanceof TransientDataAccessResourceException) {
                // 发延时队列重试
                Message<OutboundCallTaskCdr> message = MessageBuilder.withPayload(outboundCallTaskCdr)
                        .setHeader(MessageConst.PROPERTY_KEYS, outboundCallTaskCdr.getClientNumber())
                        .build();
                rocketMQTemplate.syncSend(getRetryDestination(), message, 5000L, 16);
            }
        } finally {
            RequestDataHelper.remove();
        }
    }

    private String getRetryDestination() {
        return RocketMqConstant.CLOUD_CC_OUTBOUND_CALL_TASK + StrUtil.COLON + RocketMqConstant.OUTBOUND_CALL_TASK_DB_TAG;
    }
}
