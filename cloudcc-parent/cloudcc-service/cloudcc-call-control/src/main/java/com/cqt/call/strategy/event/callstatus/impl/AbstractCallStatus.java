package com.cqt.call.strategy.event.callstatus.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.cqt.base.enums.CallInChannelEnum;
import com.cqt.base.enums.CallRoleEnum;
import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.base.enums.RecordNodeEnum;
import com.cqt.base.enums.cdr.CallInOwnRecorderEnum;
import com.cqt.base.enums.cdr.CallInPeerRecorderEnum;
import com.cqt.base.enums.cdr.CalloutOwnRecorderEnum;
import com.cqt.base.enums.cdr.CalloutPeerRecorderEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.call.converter.ModelConverter;
import com.cqt.call.event.store.ExtStatusActualStoreEvent;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.entity.ExtStatusLog;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.dto.ExtStatusTransferDTO;
import com.cqt.model.freeswitch.dto.api.RecordDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.RecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-09-14 9:50
 */
@Slf4j
public abstract class AbstractCallStatus {

    /**
     * 开始录制
     */
    public void startRecord(CallStatusEventEnum callStatusEventEnum, CallUuidContext callUuidContext) {
        String companyCode = callUuidContext.getCompanyCode();
        CompanyInfo companyInfo = getCommonDataOperateService().getCompanyInfoDTO(companyCode);
        if (Objects.isNull(companyInfo)) {
            return;
        }

        // 呼入A路录制
        if (RecordNodeEnum.CALL_IN_A.equals(callUuidContext.getRecordNode())) {
            callinRecordA(callStatusEventEnum, callUuidContext, companyInfo);
            return;
        }

        // 呼入B路录制
        if (RecordNodeEnum.CALL_IN_B.equals(callUuidContext.getRecordNode())) {
            callinRecordB(callStatusEventEnum, callUuidContext, companyInfo);
            return;
        }

        // 呼出A路录制
        if (RecordNodeEnum.CALL_OUT_A.equals(callUuidContext.getRecordNode())) {
            calloutRecordA(callStatusEventEnum, callUuidContext, companyInfo);
            return;
        }

        // 呼出B路录制
        if (RecordNodeEnum.CALL_OUT_B.equals(callUuidContext.getRecordNode())) {
            calloutRecordB(callStatusEventEnum, callUuidContext, companyInfo);
        }

    }

    private void calloutRecordA(CallStatusEventEnum callStatusEventEnum,
                                CallUuidContext callUuidContext,
                                CompanyInfo companyInfo) {
        String companyCode = companyInfo.getCompanyCode();
        Integer callOutRecordNodeA = companyInfo.getCallOutRecordNodeA();
        if (CalloutOwnRecorderEnum.NONE.getCode().equals(callOutRecordNodeA)) {
            return;
        }
        switch (callStatusEventEnum) {
            case INVITE:
                if (CalloutOwnRecorderEnum.AGENT_INVITE.getCode().equals(callOutRecordNodeA)) {
                    log.info("[录制-呼出] 企业: {}, A路 INVITE", companyCode);
                    record(companyCode, callUuidContext.getUUID());
                    return;
                }
                return;
            case MEDIA:
                if (CalloutOwnRecorderEnum.AGENT_RING.getCode().equals(callOutRecordNodeA)) {
                    log.info("[录制-呼出] 企业: {}, A路 RING", companyCode);
                    record(companyCode, callUuidContext.getUUID());
                    return;
                }
                return;
            case ANSWER:
                if (CalloutOwnRecorderEnum.AGENT_ANSWER.getCode().equals(callOutRecordNodeA)) {
                    log.info("[录制-呼出] 企业: {}, A路 ANSWER", companyCode);
                    record(companyCode, callUuidContext.getUUID());
                }
                return;
            default:
        }
    }

    private void calloutRecordB(CallStatusEventEnum callStatusEventEnum,
                                CallUuidContext callUuidContext,
                                CompanyInfo companyInfo) {
        String companyCode = companyInfo.getCompanyCode();
        Integer callOutRecordNodeB = companyInfo.getCallOutRecordNodeB();
        if (CalloutPeerRecorderEnum.NONE.getCode().equals(callOutRecordNodeB)) {
            return;
        }
        switch (callStatusEventEnum) {
            case MEDIA:
                // 客户振铃后, 录制A路
                if (isCallout(callUuidContext)
                        && CalloutOwnRecorderEnum.CLIENT_RING.getCode().equals(companyInfo.getCallOutRecordNodeA())) {
                    log.info("[录制-呼出] 企业: {}, A路 对方RING", companyCode);
                    record(companyCode, callUuidContext.findRelationUUID());
                }
                if (CalloutPeerRecorderEnum.RING.getCode().equals(callOutRecordNodeB)) {
                    log.info("[录制-呼出] 企业: {}, B路 RING", companyCode);
                    // 录制B路
                    record(companyCode, callUuidContext.getUUID());
                }
                return;
            case ANSWER:
                // 客户接通后, 录制A路
                if (isCallout(callUuidContext)
                        && CalloutOwnRecorderEnum.CLIENT_ANSWER.getCode().equals(companyInfo.getCallOutRecordNodeA())) {
                    log.info("[录制-呼出] 企业: {}, A路 对方ANSWER", companyCode);
                    record(companyCode, callUuidContext.findRelationUUID());
                }
                if (CalloutPeerRecorderEnum.ANSWER.getCode().equals(callOutRecordNodeB)) {
                    log.info("[录制-呼出] 企业: {}, B路 ANSWER", companyCode);
                    record(companyCode, callUuidContext.getUUID());
                }
                return;
            default:
        }
    }

    private boolean isCallout(CallUuidContext callUuidContext) {
        return CallInChannelEnum.CALL.equals(callUuidContext.getCallInChannel())
                || CallInChannelEnum.PREDICT_OUT_CALL.equals(callUuidContext.getCallInChannel())
                || CallInChannelEnum.PREVIEW_OUT_CALL.equals(callUuidContext.getCallInChannel());
    }

    private void callinRecordA(CallStatusEventEnum callStatusEventEnum,
                               CallUuidContext callUuidContext,
                               CompanyInfo companyInfo) {
        if (CallStatusEventEnum.ANSWER.equals(callStatusEventEnum)
                && Boolean.TRUE.equals(callUuidContext.getCallinIVR())) {
            String companyCode = companyInfo.getCompanyCode();
            Integer callInRecordNodeA = companyInfo.getCallInRecordNodeA();
            if (CallInPeerRecorderEnum.NONE.getCode().equals(callInRecordNodeA)) {
                return;
            }
            if (Objects.equals(callInRecordNodeA, CallInOwnRecorderEnum.CALL_IN.getCode())) {
                log.info("[录制-呼入] 企业: {}, A路", companyCode);
                record(companyCode, callUuidContext.getUUID());
            }
        }
    }

    /**
     * 呼入B路录制处理
     */
    private void callinRecordB(CallStatusEventEnum callStatusEventEnum,
                               CallUuidContext callUuidContext,
                               CompanyInfo companyInfo) {
        String companyCode = companyInfo.getCompanyCode();
        if (CallRoleEnum.CALLIN_TRANSFER_AGENT.equals(callUuidContext.getCallRoleEnum())
                || CallRoleEnum.CALLIN_TRANSFER_OFFLINE_AGENT_PHONE.equals(callUuidContext.getCallRoleEnum())) {
            Integer callInRecordNodeB = companyInfo.getCallInRecordNodeB();
            if (CallInPeerRecorderEnum.NONE.getCode().equals(callInRecordNodeB)) {
                return;
            }
            String uuid = callUuidContext.getUUID();
            switch (callStatusEventEnum) {
                case MEDIA:
                    if (CallInPeerRecorderEnum.AGENT_RING.getCode().equals(callInRecordNodeB)) {
                        log.info("[录制-呼入] 企业: {}, B路 RING", companyCode);
                        record(companyCode, uuid);
                        return;
                    }
                    return;
                case ANSWER:
                    if (CallInPeerRecorderEnum.AGENT_ANSWER.getCode().equals(callInRecordNodeB)) {
                        log.info("[录制-呼入] 企业: {}, B路 ANSWER", companyCode);
                        record(companyCode, uuid);
                    }
                    return;
                default:
            }
        }
    }

    private void record(String companyCode, String uuid) {
        RecordDTO recordDTO = RecordDTO.build(companyCode, uuid);
        RecordVO recordVO = getFreeswitchRequestService().record(recordDTO);
        getCommonDataOperateService().saveRecordFile(companyCode, uuid, recordVO.getRecordFileName());
    }

    /**
     * 分机状态迁移
     *
     * @param callStatusEventDTO 事件
     * @param callUuidContext    uuid上下文
     */
    public void extStatusTransfer(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        // 分机状态 外呼中->振铃中
        ExtStatusTransferDTO transferDTO = buildExtStatusTransferDTO(callStatusEventDTO, callUuidContext);
        // 状态迁移日志
        ExtStatusLog extStatusLog = ModelConverter.INSTANCE.extStatusTransfer2ExtStatusLog(transferDTO);
        getApplicationContext().publishEvent(new ExtStatusActualStoreEvent(this, extStatusLog));
    }

    /**
     * 构建分机状态迁移日志
     *
     * @param callStatusEventDTO 事件
     * @param callUuidContext    uuid上下文
     * @return 分机状态迁移实体
     */
    public ExtStatusTransferDTO buildExtStatusTransferDTO(CallStatusEventDTO callStatusEventDTO,
                                                          CallUuidContext callUuidContext) {
        // 查分机状态
        String companyCode = callStatusEventDTO.getCompanyCode();
        String extId = callUuidContext.getExtId();
        ExtStatusDTO extStatusDTO = getCommonDataOperateService().getActualExtStatus(companyCode, extId);
        ExtStatusTransferDTO transferDTO = new ExtStatusTransferDTO();
        transferDTO.setCompanyCode(companyCode);
        transferDTO.setExtId(extId);
        transferDTO.setExtIp(callUuidContext.getExtIp());
        transferDTO.setAgentId(callUuidContext.getAgentId());
        transferDTO.setUuid(callUuidContext.getUUID());
        if (Objects.nonNull(extStatusDTO)) {
            transferDTO.setSourceStatus(extStatusDTO.getTargetStatus());
            transferDTO.setSourceTimestamp(extStatusDTO.getTargetTimestamp());
        }
        transferDTO.setTransferAction(getExtStatusTransferActionEnum().name());
        transferDTO.setTargetStatus(getExtStatusEnum().name());
        transferDTO.setTargetTimestamp(callStatusEventDTO.getTimestamp());
        return transferDTO;
    }

    private FreeswitchRequestService getFreeswitchRequestService() {
        return SpringUtil.getBean(FreeswitchRequestService.class);
    }

    public abstract ApplicationContext getApplicationContext();

    public abstract CommonDataOperateService getCommonDataOperateService();

    public abstract ExtStatusTransferActionEnum getExtStatusTransferActionEnum();

    public abstract ExtStatusEnum getExtStatusEnum();
}
