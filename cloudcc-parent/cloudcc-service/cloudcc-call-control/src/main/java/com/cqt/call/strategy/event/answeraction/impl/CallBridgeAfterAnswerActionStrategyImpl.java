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
import com.cqt.model.client.dto.ClientCallDTO;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.dto.api.CallBridgeDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 外呼并桥接客户, 调用底层call_bridge接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallBridgeAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.CALL_BRIDGE_CLIENT;
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
        final CallStatusEventDTO.EventData eventData = callStatusEventDTO.getData();
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallUuidRelationDTO current = callUuidContext.getCurrent();

        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        // 暂定随机uuid, 或可设置通话关联的信息
        String reqId = IdUtil.fastUUID();
        callBridgeDTO.setReqId(reqId);
        callBridgeDTO.setCompanyCode(companyCode);
        callBridgeDTO.setSUuid(uuid);
        // 暂时做A路uuid前加B-
        String clientUuid = "B-" + uuid;
        callBridgeDTO.setOriUuid(clientUuid);
        callBridgeDTO.setServerId(callStatusEventDTO.getServerId());
        // 查询企业和坐席配置信息(发起外呼的)
        CompanyInfo companyInfo = commonDataOperateService.getCompanyInfoDTO(companyCode);

        // 主叫号码 - 坐席的外显号(若没配置, 传企业主显号)  打外线 查企业的主线号, 内线显示分机id
        Integer outLine = current.getClientCallDTO().getOutLine();
        String displayNumber = callUuidContext.getDisplayNumber();
        CallRoleEnum callRole = CallRoleEnum.CLIENT_CALLEE;
        CallTypeEnum callType = CallTypeEnum.CLIENT;
        String bridgeAgentId = "";
        String bridgeExtId = "";
        boolean inline = false;
        // 内线
        if (OutLineEnum.IN_LINE.getCode().equals(outLine)) {
            displayNumber = current.getClientCallDTO().getExtId();
            callRole = CallRoleEnum.AGENT_CALLEE;
            callType = CallTypeEnum.AGENT;
            bridgeExtId = eventData.getCallerNumber();
            // 根据分机查绑定的坐席id
            bridgeAgentId = commonDataOperateService.getAgentIdRelateExtId(companyCode, bridgeExtId);
            inline = true;
        }
        callBridgeDTO.setCallerNumber(callUuidContext.getExtId());
        callBridgeDTO.setDisplayNumber(displayNumber);

        // 被叫号码
        callBridgeDTO.setCalleeNumber(callUuidContext.getCalleeNumber());
        callBridgeDTO.setOutLine(outLine);
        ClientCallDTO clientCallDTO = current.getClientCallDTO();
        if (Objects.nonNull(clientCallDTO)) {
            callBridgeDTO.setClientCallParams(clientCallDTO);
        }
        // 企业关于坐席的一些配置
        if (Objects.nonNull(companyInfo)) {
            configAgent(callBridgeDTO, companyInfo);
        }

        // 5. 保证客户的uuid信息 和坐席的uuid关联客户uuid信息
        // 先写redis 再桥接
        CallUuidRelationDTO bridgeCallUuidRelationDTO = new CallUuidRelationDTO();
        bridgeCallUuidRelationDTO.setMainCdrFlag(false);
        bridgeCallUuidRelationDTO.setMainCallId(current.getMainCallId());
        bridgeCallUuidRelationDTO.setUuid(clientUuid);
        bridgeCallUuidRelationDTO.setMainUuid(uuid);
        bridgeCallUuidRelationDTO.setRelationUuid(uuid);
        bridgeCallUuidRelationDTO.setNumber(eventData.getCallerNumber());
        bridgeCallUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.OUTBOUND);
        bridgeCallUuidRelationDTO.setCallRoleEnum(callRole);
        bridgeCallUuidRelationDTO.setCallTypeEnum(callType);
        bridgeCallUuidRelationDTO.setOs(current.getOs());
        bridgeCallUuidRelationDTO.setExtId(bridgeExtId);
        bridgeCallUuidRelationDTO.setAgentId(bridgeAgentId);
        bridgeCallUuidRelationDTO.setExtIp("");
        bridgeCallUuidRelationDTO.setReqId(reqId);
        bridgeCallUuidRelationDTO.setCallerNumber(callUuidContext.getExtId());
        bridgeCallUuidRelationDTO.setDisplayNumber(displayNumber);
        bridgeCallUuidRelationDTO.setCalleeNumber(callUuidContext.getCalleeNumber());
        // 若是桥接内线 查询下坐席的信息
        bridgeCallUuidRelationDTO.setCompanyCode(companyCode);
        bridgeCallUuidRelationDTO.setServerId(callStatusEventDTO.getServerId());
        bridgeCallUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.NONE);
        bridgeCallUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        bridgeCallUuidRelationDTO.setVideo(current.getClientCallDTO().getVideo());
        bridgeCallUuidRelationDTO.setAudio(current.getClientCallDTO().getAudio());
        bridgeCallUuidRelationDTO.setCallInFlag(inline);
        bridgeCallUuidRelationDTO.setCallInChannel(CallInChannelEnum.CALL);
        bridgeCallUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        // 当前上下文-坐席, 保存关联客户uuid
        current.setRelationUuid(clientUuid);
        callUuidContext.fillRelationUuidSet(clientUuid);
        callUuidContext.fillRelateUuidDtoByCallBridge(clientUuid);
        // 子话单uuid
        commonDataOperateService.saveCallUuidContext(callUuidContext);
        commonDataOperateService.saveCdrLink(companyCode, current.getMainCallId(), uuid, clientUuid);

        // 客户uuid上下文-坐席外呼并桥接被叫
        CallUuidContext clientCallUuidContext = CallUuidContext.builder()
                .current(bridgeCallUuidRelationDTO)
                // 客户, 保存关联坐席uuid
                .relationUuid(Sets.newHashSet(uuid))
                .relateUuidDTO(RelateUuidDTO.builder().bridgeUUID(uuid).build())
                .build();
        commonDataOperateService.saveCallUuidContext(clientCallUuidContext);

        boolean callBridgeSuccess = false;
        String resultMsg = "外呼成功!";
        try {
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.callBridge(callBridgeDTO);
            if (freeswitchApiVO.getResult()) {
                callBridgeSuccess = true;
                if (log.isInfoEnabled()) {
                    log.info("[接通事件-callBridge] 企业: {}, callBridgeDTO: {}, 桥接成功",
                            companyCode, objectMapper.writeValueAsString(callBridgeDTO));
                }
            }
            resultMsg = freeswitchApiVO.getMsg();
            // 是否根据返回结果判断是否桥接成功,
            //      呼叫失败:-ERR USER_BUSY 这种情况, 主叫未自动挂断, 是否需要手动挂断
        } catch (Exception e) {
            log.error("[接通事件] 企业id: {}, 事件内容: {}, 调用callBridge接口异常, 挂断当前坐席通话并通知SDK",
                    companyCode, callStatusEventDTO, e);
        }
        if (!callBridgeSuccess) {
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.CALL_BRIDGE_FAIL);
            // 桥接失败挂断当前坐席通话
            freeswitchRequestService.hangup(hangupDTO);
        }
        ClientCallbackVO vo = ClientCallbackVO.buildInit(callUuidContext, CallbackActionEnum.CALL, callBridgeSuccess, resultMsg);
        dataStoreService.notifyClient(vo);
    }

    /**
     * 关于坐席的一些配置
     */
    private void configAgent(CallBridgeDTO callBridgeDTO, CompanyInfo companyInfo) {
        callBridgeDTO.setMaxRingTime(companyInfo.getCallOutRingTimeout());
    }
}
