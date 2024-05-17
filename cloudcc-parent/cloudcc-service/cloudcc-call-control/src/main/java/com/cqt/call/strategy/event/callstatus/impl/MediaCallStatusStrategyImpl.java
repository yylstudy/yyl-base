package com.cqt.call.strategy.event.callstatus.impl;

import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-11-20 13:37
 * 媒体建立
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaCallStatusStrategyImpl extends AbstractCallStatus implements CallStatusStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataQueryService dataQueryService;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    @Override
    public void deal(CallStatusEventDTO callStatusEventDTO) throws Exception {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            log.info("[媒体建立事件] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("[媒体建立事件] 当前uuid上下文信息: {}", objectMapper.writeValueAsString(callUuidContext));
        }
        // 开始录制处理
        startRecord(getCallStatus(), callUuidContext);
    }

    @Override
    public CallStatusEventEnum getCallStatus() {
        return CallStatusEventEnum.MEDIA;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public CommonDataOperateService getCommonDataOperateService() {
        return commonDataOperateService;
    }

    @Override
    public ExtStatusTransferActionEnum getExtStatusTransferActionEnum() {
        return ExtStatusTransferActionEnum.RING;
    }

    @Override
    public ExtStatusEnum getExtStatusEnum() {
        return ExtStatusEnum.RINGING;
    }
}
