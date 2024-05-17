package com.cqt.call.strategy.event.answeraction.impl;

import com.cqt.base.enums.OriginateAfterActionEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.freeswitch.dto.api.BridgeDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 管理员代接
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubstituteAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.SUBSTITUTE;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        substituteAgent(callStatusEventDTO, callUuidContext);
    }

    /**
     * 代接坐席
     *
     * @param callStatusEventDTO 通话事件消息
     * @param callUuidContext    当前事件 uuid上下文
     */
    private void substituteAgent(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        CallUuidRelationDTO current = callUuidContext.getCurrent();
        String companyCode = callStatusEventDTO.getCompanyCode();
        String mainUUID = callUuidContext.getMainUUID();
        // 被代接的坐席
        CallUuidContext agentCallUuidContext = commonDataOperateService.getCallUuidContext(companyCode, mainUUID);
        commonDataOperateService.saveCallUuidContext(agentCallUuidContext);
        String relationUuid = agentCallUuidContext.getCurrent().getRelationUuid();
        commonDataOperateService.saveCdrLink(companyCode, current.getMainCallId(), callStatusEventDTO.getUuid(), relationUuid);

        //  挂断原坐席
        HangupDTO hangupDTO = HangupDTO.build(companyCode, current.getMainUuid(), HangupCauseEnum.SUBSTITUTE);
        FreeswitchApiVO hangupVO = freeswitchRequestService.hangup(hangupDTO);
        log.info("[接通事件-代接] 企业id: {}, 挂断uuid: {}", companyCode, current.getMainUuid());
        //  桥接管理员坐席和客户
        if (hangupVO.getResult()) {
            BridgeDTO bridgeDTO = BridgeDTO.build(companyCode, current.getRelationUuid(), current.getUuid());
            freeswitchRequestService.bridge(bridgeDTO);
            log.info("[接通事件-代接] 企业id: {}, 桥接管理员: {}和原通话: {}", companyCode, current.getRelationUuid(), current.getUuid());
        }
    }
}
