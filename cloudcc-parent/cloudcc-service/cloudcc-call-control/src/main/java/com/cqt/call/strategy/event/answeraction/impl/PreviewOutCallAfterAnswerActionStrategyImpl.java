package com.cqt.call.strategy.event.answeraction.impl;

import cn.hutool.core.util.IdUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.agent.vo.RelateUuidDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.freeswitch.dto.api.CallBridgeDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 预览外呼 外呼并桥接客户, 调用底层call_bridge接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewOutCallAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.PREVIEW_TASK;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        callBridgeClient(callStatusEventDTO, callUuidContext);
    }

    /**
     * 外呼并桥接
     *
     * @param callStatusEventDTO 当前事件消息
     * @param callUuidContext    当前uuid上下文-坐席
     */
    private void callBridgeClient(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();

        CallBridgeDTO callBridgeDTO = buildCallBridge(callStatusEventDTO, callUuidContext);
        writeClientContext(callStatusEventDTO, callUuidContext, callBridgeDTO);
        boolean callBridgeSuccess = false;
        String resultMsg = "预览外呼成功!";
        try {
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.callBridge(callBridgeDTO);
            if (freeswitchApiVO.getResult()) {
                callBridgeSuccess = true;
                if (log.isInfoEnabled()) {
                    log.info("[接通事件-预览外呼] 企业: {}, callBridgeDTO: {}, 桥接成功",
                            companyCode, objectMapper.writeValueAsString(callBridgeDTO));
                }
            }
            resultMsg = freeswitchApiVO.getMsg();
            // 是否根据返回结果判断是否桥接成功,
            //      呼叫失败:-ERR USER_BUSY 这种情况, 主叫未自动挂断, 是否需要手动挂断
        } catch (Exception e) {
            log.error("[接通事件-预览外呼] 企业id: {}, 事件内容: {}, 调用callBridge接口异常, 挂断当前坐席通话并通知SDK",
                    companyCode, callStatusEventDTO, e);
        }
        if (!callBridgeSuccess) {
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.PREVIEW_OUT_CALL_BRIDGE_FAIL);
            // 桥接失败挂断当前坐席通话
            freeswitchRequestService.hangup(hangupDTO);
        }
        ClientCallbackVO vo = ClientCallbackVO.buildInit(callUuidContext, CallbackActionEnum.PREVIEW_OUT_CALL,
                callBridgeSuccess, resultMsg);
        dataStoreService.notifyClient(vo);
    }

    private CallBridgeDTO buildCallBridge(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        ClientPreviewOutCallDTO previewOutCallDTO = callUuidContext.getClientPreviewOutCallDTO();
        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        callBridgeDTO.setReqId(IdUtil.fastUUID());
        callBridgeDTO.setCompanyCode(callStatusEventDTO.getCompanyCode());
        callBridgeDTO.setSUuid(callStatusEventDTO.getUuid());
        // 暂时做A路uuid前加B-
        String clientUuid = "B-" + callStatusEventDTO.getUuid();
        callBridgeDTO.setOriUuid(clientUuid);
        callBridgeDTO.setServerId(callStatusEventDTO.getServerId());
        String callClientDisplayNumber = callUuidContext.getCurrent().getCallBridgeDisplayNumber();
        callBridgeDTO.setCallerNumber(callUuidContext.getExtId());
        callBridgeDTO.setDisplayNumber(callClientDisplayNumber);
        callBridgeDTO.setCalleeNumber(previewOutCallDTO.getClientNumber());
        callBridgeDTO.setOutLine(OutLineEnum.OUT_LINE.getCode());
        callBridgeDTO.setClientCallParams(previewOutCallDTO);
        return callBridgeDTO;
    }

    private void writeClientContext(CallStatusEventDTO callStatusEventDTO,
                                    CallUuidContext agentContext,
                                    CallBridgeDTO callBridgeDTO) {

        ClientPreviewOutCallDTO previewOutCallDTO = agentContext.getClientPreviewOutCallDTO();

        CallUuidRelationDTO bridgeCallUuidRelationDTO = new CallUuidRelationDTO();
        bridgeCallUuidRelationDTO.setMainCdrFlag(false);
        bridgeCallUuidRelationDTO.setMainCallId(agentContext.getMainCallId());
        bridgeCallUuidRelationDTO.setUuid(callBridgeDTO.getOriUuid());
        bridgeCallUuidRelationDTO.setMainUuid(agentContext.getMainUUID());
        bridgeCallUuidRelationDTO.setRelationUuid(agentContext.getUUID());
        bridgeCallUuidRelationDTO.setNumber(callBridgeDTO.getCalleeNumber());
        bridgeCallUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.OUTBOUND);
        bridgeCallUuidRelationDTO.setCallRoleEnum(CallRoleEnum.CLIENT_CALLEE);
        bridgeCallUuidRelationDTO.setCallTypeEnum(CallTypeEnum.CLIENT);
        bridgeCallUuidRelationDTO.setExtId(previewOutCallDTO.getExtId());
        bridgeCallUuidRelationDTO.setAgentId(previewOutCallDTO.getAgentId());
        bridgeCallUuidRelationDTO.setExtIp("");
        bridgeCallUuidRelationDTO.setReqId(previewOutCallDTO.getReqId());
        bridgeCallUuidRelationDTO.setCallerNumber(callBridgeDTO.getCallerNumber());
        bridgeCallUuidRelationDTO.setDisplayNumber(callBridgeDTO.getDisplayNumber());
        bridgeCallUuidRelationDTO.setCalleeNumber(callBridgeDTO.getCalleeNumber());
        // 若是桥接内线 查询下坐席的信息
        bridgeCallUuidRelationDTO.setCompanyCode(callStatusEventDTO.getCompanyCode());
        bridgeCallUuidRelationDTO.setServerId(callStatusEventDTO.getServerId());
        bridgeCallUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        bridgeCallUuidRelationDTO.setVideo(agentContext.getVideo());
        bridgeCallUuidRelationDTO.setAudio(agentContext.getAudio());
        bridgeCallUuidRelationDTO.setCallInFlag(false);
        bridgeCallUuidRelationDTO.setCallInChannel(CallInChannelEnum.PREVIEW_OUT_CALL);
        bridgeCallUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        String agentUuid = callBridgeDTO.getSUuid();
        String clientUuid = callBridgeDTO.getOriUuid();

        // 客户uuid上下文-坐席外呼并桥接被叫
        CallUuidContext clientContext = CallUuidContext.builder()
                .current(bridgeCallUuidRelationDTO)
                // 客户, 保存关联坐席uuid
                .relationUuid(Sets.newHashSet(agentUuid))
                .relateUuidDTO(RelateUuidDTO.builder().bridgeUUID(agentUuid).build())
                .build();
        commonDataOperateService.saveCallUuidContext(clientContext);

        // 当前上下文-坐席, 保存关联客户uuid
        agentContext.getCurrent().setRelationUuid(clientUuid);
        agentContext.fillRelationUuidSet(clientUuid);
        agentContext.fillRelateUuidDtoByCallBridge(clientUuid);
        commonDataOperateService.saveCallUuidContext(agentContext);

        // 子话单uuid
        commonDataOperateService.saveCdrLink(agentContext.getCompanyCode(), agentContext.getMainCallId(), agentUuid, clientUuid);
    }

}
