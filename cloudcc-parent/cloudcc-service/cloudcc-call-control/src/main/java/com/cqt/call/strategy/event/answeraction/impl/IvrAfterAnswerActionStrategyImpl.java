package com.cqt.call.strategy.event.answeraction.impl;

import cn.hutool.core.text.StrFormatter;
import com.cqt.base.contants.SystemConstant;
import com.cqt.base.enums.OriginateAfterActionEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.freeswitch.dto.api.ExecuteLuaDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 执行ivr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IvrAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.IVR;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        ClientOutboundCallTaskDTO clientOutboundCallTaskDTO = callUuidContext.getClientOutboundCallTaskDTO();

        String taskId = clientOutboundCallTaskDTO.getTaskId();
        String member = clientOutboundCallTaskDTO.getMember();
        dataStoreService.answerIvrNotice(clientOutboundCallTaskDTO.getCompanyCode(), taskId, member);

        executeIvr(callStatusEventDTO, clientOutboundCallTaskDTO.getIvrId());
    }

    /**
     * 坐席接通 执行ivr
     *
     * @param callStatusEventDTO 当前事件消息
     * @param ivrId    ivrId
     */
    private void executeIvr(CallStatusEventDTO callStatusEventDTO, String ivrId) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        String luaName = StrFormatter.format(SystemConstant.LUA_TEMPLATE, ivrId);
        ExecuteLuaDTO executeLuaDTO = ExecuteLuaDTO.build(companyCode, uuid, luaName);
        freeswitchRequestService.executeLua(executeLuaDTO);
    }
}
