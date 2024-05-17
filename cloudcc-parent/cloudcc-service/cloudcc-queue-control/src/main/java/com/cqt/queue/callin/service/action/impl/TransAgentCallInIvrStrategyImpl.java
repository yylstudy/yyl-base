package com.cqt.queue.callin.service.action.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.model.ResultVO;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.freeswitch.dto.api.CallBridgeDTO;
import com.cqt.model.freeswitch.dto.api.CallQueueToAgentDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.queue.callin.service.action.CallInIvrStrategy;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-08-21 10:26
 * 转接坐席
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransAgentCallInIvrStrategyImpl implements CallInIvrStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public CallInIvrActionEnum getAction() {
        return CallInIvrActionEnum.IVR_TRANS_AGENT;
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) throws Exception {
        String companyCode = callInIvrActionDTO.getCompanyCode();
        String agentId = callInIvrActionDTO.getAgentId();
        String extId = commonDataOperateService.getExtIdRelateAgentId(companyCode, agentId);
        String uuid = callInIvrActionDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        String oriUuid = getOriUuid(uuid);
        CallBridgeDTO callBridgeDTO = CallBridgeDTO.build(callUuidContext, callInIvrActionDTO, oriUuid, extId,
                OutLineEnum.IN_LINE);

        // 保存context
        writeCallUuidContext(callInIvrActionDTO, callUuidContext, callBridgeDTO);
        log.info("[呼入ivr-坐席] 转接坐席: {}, 分机: {}, 企业: {}, 来电号码: {}",
                agentId, extId, companyCode, callInIvrActionDTO.getCallerNumber());
        CallQueueToAgentDTO callQueueToAgentDTO = CallQueueToAgentDTO.build(IdUtil.fastUUID(), companyCode, uuid, extId);
        FreeswitchApiVO apiVO = freeswitchRequestService.callQueueToAgent(callQueueToAgentDTO);
        if (apiVO.getResult()) {
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.callBridge(callBridgeDTO);
            if (freeswitchApiVO.getResult()) {
                commonDataOperateService.saveCdrLink(companyCode, callUuidContext.getMainCallId(), uuid, oriUuid);
                return ResultVO.ok();
            }
        }
        return ResultVO.fail(400, apiVO.getMsg());
    }

    private String getOriUuid(String uuid) {
        return getAction().getName() + StrUtil.DASHED + uuid;
    }

    /**
     * 坐席uuid上下文携入redis
     *
     * @param callInIvrActionDTO 呼入ivr参数
     * @param callUuidContext    来电uuid上下文-客户
     * @param callBridgeDTO      外呼并桥接参数
     */
    private void writeCallUuidContext(CallInIvrActionDTO callInIvrActionDTO,
                                      CallUuidContext callUuidContext,
                                      CallBridgeDTO callBridgeDTO) {
        String companyCode = callInIvrActionDTO.getCompanyCode();
        String agentId = callInIvrActionDTO.getAgentId();
        Optional<AgentStatusDTO> statusDtoOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setUuid(callBridgeDTO.getOriUuid());
        callUuidRelationDTO.setMainCallId(callUuidContext.getMainCallId());
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setHangupAll(true);
        callUuidRelationDTO.setRelationUuid(callUuidContext.getUUID());
        callUuidRelationDTO.setMainUuid(callUuidContext.getUUID());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.CALL_IN_IVR_TRANS_AGENT);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setExtId(callBridgeDTO.getCalleeNumber());
        callUuidRelationDTO.setNumber(callBridgeDTO.getCalleeNumber());
        callUuidRelationDTO.setAgentId(callInIvrActionDTO.getAgentId());
        // 呼入属性
        callUuidRelationDTO.setVideo(callUuidContext.getVideo());
        callUuidRelationDTO.setAudio(callUuidContext.getAudio());
        callUuidRelationDTO.setCallInFlag(true);
        callUuidRelationDTO.setCallInChannel(CallInChannelEnum.TRANS);
        callUuidRelationDTO.setReqId(callBridgeDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(callBridgeDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(callBridgeDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(callBridgeDTO.getCalleeNumber());
        callUuidRelationDTO.setCompanyCode(callInIvrActionDTO.getCompanyCode());
        callUuidRelationDTO.setServerId(callUuidContext.getCurrent().getServerId());
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.INBOUND);
        if (statusDtoOptional.isPresent()) {
            AgentStatusDTO agentStatusDTO = statusDtoOptional.get();
            callUuidRelationDTO.setOs(agentStatusDTO.getOs());
            callUuidRelationDTO.setExtIp(callUuidRelationDTO.getExtIp());
        }
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callCdrDTO.setUuid(callBridgeDTO.getOriUuid());
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        // 转接坐席
        CallUuidContext agentCallUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relationUuid(Sets.newHashSet(callBridgeDTO.getSUuid()))
                .build();
        commonDataOperateService.saveCallUuidContext(agentCallUuidContext);

        // 呼入客户
        callUuidContext.fillRelateUuidDtoByCallBridge(callBridgeDTO.getOriUuid());
        callUuidContext.fillRelationUuidSet(callBridgeDTO.getOriUuid());
        callUuidContext.getCurrent().setRelationUuid(callBridgeDTO.getOriUuid());
        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }
}
