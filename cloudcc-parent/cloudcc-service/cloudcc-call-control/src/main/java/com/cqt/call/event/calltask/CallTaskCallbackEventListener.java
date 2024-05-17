package com.cqt.call.event.calltask;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.cloudcc.manager.config.CommonThreadPoolConfig;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.calltask.entity.OutboundCallTaskCdr;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-23 15:55
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallTaskCallbackEventListener implements ApplicationListener<CallTaskCallbackEvent> {

    private final RocketMQTemplate rocketMQTemplate;

    private final CommonDataOperateService commonDataOperateService;

    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @Override
    public void onApplicationEvent(CallTaskCallbackEvent event) {
        CallUuidContext clientContext = event.getClientContext();
        CallUuidContext agentContext = event.getAgentContext();
        CallTaskEnum callTaskEnum = event.getCallTaskEnum();
        OutboundCallTaskCdr taskCdr;
        if (CallTaskEnum.PREVIEW.equals(callTaskEnum)) {
            taskCdr = buildPreviewTaskCallback(clientContext, agentContext);
        } else {
            taskCdr = buildCallback(clientContext, agentContext);
        }
        taskCdr.setTaskType(callTaskEnum.getCode());
        try {
            // 发mq进行入库
            Message<OutboundCallTaskCdr> message = MessageBuilder.withPayload(taskCdr)
                    .setHeader(MessageConst.PROPERTY_KEYS, taskCdr.getClientNumber())
                    .build();
            SendResult sendResult = rocketMQTemplate.syncSend(getDestination(), message);
            log.debug("[外呼任务结果回调] 发送mq结果: {}", sendResult);
        } catch (Exception e) {
            // TODO 发送失败
            log.error("[外呼任务结果回调] data: {}, 发送mq异常: ", taskCdr, e);
        }
    }

    private String getDestination() {
        return RocketMqConstant.CLOUD_CC_OUTBOUND_CALL_TASK + StrUtil.COLON + RocketMqConstant.OUTBOUND_CALL_TASK_DB_TAG;
    }

    private OutboundCallTaskCdr buildPreviewTaskCallback(CallUuidContext clientContext, CallUuidContext agentContext) {
        CallCdrDTO callCdrDTO = agentContext.getCallCdrDTO();
        ClientPreviewOutCallDTO previewOutCallDTO = agentContext.getClientPreviewOutCallDTO();
        OutboundCallTaskCdr taskCdr = new OutboundCallTaskCdr();
        taskCdr.setCompanyCode(agentContext.getCompanyCode());
        taskCdr.setMainCallId(agentContext.getMainCallId());
        taskCdr.setSubUuid(agentContext.getUUID());
        taskCdr.setCallStartTime(getTime(callCdrDTO.getInviteTimestamp()));
        taskCdr.setClientAnswerTime(getTime(callCdrDTO.getAnswerTimestamp()));
        taskCdr.setHangupTime(getTime(callCdrDTO.getHangupTimestamp()));
        taskCdr.setDuration(getCallDuration(callCdrDTO.getAnswerTimestamp(), callCdrDTO.getHangupTimestamp()));
        // 平台号码
        taskCdr.setDisplayNumber(agentContext.getCurrent().getPlatformNumber());
        // 客户号码
        taskCdr.setClientNumber(previewOutCallDTO.getClientNumber());
        taskCdr.setNumberId(previewOutCallDTO.getNumberId());
        taskCdr.setTaskId(previewOutCallDTO.getTaskId());
        taskCdr.setCurrentTimes(previewOutCallDTO.getCurrentTimes());
        taskCdr.setAnswerStatus(getState(callCdrDTO));
        taskCdr.setAgentId(agentContext.getAgentId());
        taskCdr.setAgentAnswerTime(getTime(agentContext.getCallCdrDTO().getAnswerTimestamp()));
        taskCdr.setRecordUrl(commonDataOperateService.getRecordFile(agentContext.getCompanyCode(), agentContext.getUUID()));
        taskCdr.setServiceId(agentContext.getServerId());
        if (!isAnswer(callCdrDTO)) {
            taskCdr.setFailCause("坐席未接");
        }
        if (Objects.nonNull(clientContext)) {
            // 客户是否接
            taskCdr.setSubUuid(clientContext.getUUID());
            taskCdr.setClientAnswerTime(getTime(clientContext.getCallCdrDTO().getAnswerTimestamp()));
            if (!isAnswer(clientContext.getCallCdrDTO())) {
                taskCdr.setFailCause("客户未接");
            }
        }
        return taskCdr;
    }

    private OutboundCallTaskCdr buildCallback(CallUuidContext clientContext, CallUuidContext agentContext) {
        CallCdrDTO callCdrDTO = clientContext.getCallCdrDTO();
        ClientOutboundCallTaskDTO clientOutboundCallTaskDTO = clientContext.getClientOutboundCallTaskDTO();
        OutboundCallTaskCdr taskCdr = new OutboundCallTaskCdr();
        taskCdr.setCompanyCode(clientContext.getCompanyCode());
        taskCdr.setMainCallId(clientContext.getMainCallId());
        taskCdr.setSubUuid(clientContext.getUUID());
        taskCdr.setCallStartTime(getTime(callCdrDTO.getInviteTimestamp()));
        taskCdr.setClientAnswerTime(getTime(callCdrDTO.getAnswerTimestamp()));
        taskCdr.setHangupTime(getTime(callCdrDTO.getHangupTimestamp()));
        taskCdr.setDuration(getCallDuration(callCdrDTO.getAnswerTimestamp(), callCdrDTO.getHangupTimestamp()));
        taskCdr.setTaskId(clientOutboundCallTaskDTO.getTaskId());
        taskCdr.setCallerNumber(clientOutboundCallTaskDTO.getCallerNumber());
        // 平台号码
        taskCdr.setDisplayNumber(clientOutboundCallTaskDTO.getPlatformNumber());
        // 客户号码
        taskCdr.setClientNumber(clientOutboundCallTaskDTO.getClientNumber());
        taskCdr.setNumberId(clientOutboundCallTaskDTO.getNumberId());
        taskCdr.setCurrentTimes(clientOutboundCallTaskDTO.getCurrentTimes());
        taskCdr.setAnswerStatus(getState(callCdrDTO));
        taskCdr.setRecordUrl(commonDataOperateService.getRecordFile(clientContext.getCompanyCode(), clientContext.getUUID()));
        taskCdr.setServiceId(clientContext.getServerId());
        if (!isAnswer(callCdrDTO)) {
            taskCdr.setFailCause("客户未接");
        }
        if (Objects.nonNull(agentContext)) {
            // 坐席id
            taskCdr.setAgentId(agentContext.getAgentId());
            taskCdr.setSubUuid(agentContext.getUUID());
            // 坐席是否接
            taskCdr.setAgentAnswerTime(getTime(agentContext.getCallCdrDTO().getAnswerTimestamp()));
            if (!isAnswer(agentContext.getCallCdrDTO())) {
                taskCdr.setFailCause("坐席未接");
            }
        }
        return taskCdr;
    }

    private Integer getState(CallCdrDTO callCdrDTO) {
        // 1-接通, 0-未接通, null-未开始
        return isAnswer(callCdrDTO) ? 1 : 0;
    }

    private boolean isAnswer(CallCdrDTO callCdrDTO) {
        return BooleanUtils.isTrue(callCdrDTO.getAnswerFlag());
    }

    private Long getCallDuration(Long answerTimestamp, Long hangupTimestamp) {
        if (Objects.nonNull(answerTimestamp) && Objects.nonNull(hangupTimestamp)) {
            return (hangupTimestamp - answerTimestamp) / 1000;
        }
        return null;
    }

    private Date getTime(Long timestamp) {
        return Objects.nonNull(timestamp) ? DateUtil.date(timestamp) : null;
    }
}
