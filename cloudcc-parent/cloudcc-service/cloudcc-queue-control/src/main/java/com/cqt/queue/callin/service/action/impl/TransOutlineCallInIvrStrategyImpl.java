package com.cqt.queue.callin.service.action.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.model.ResultVO;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.company.entity.CompanyInfo;
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

/**
 * @author linshiqiang
 * date:  2023-08-21 10:26
 * 转接外线
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransOutlineCallInIvrStrategyImpl implements CallInIvrStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public CallInIvrActionEnum getAction() {
        return CallInIvrActionEnum.IVR_TRANS_OUTLINE;
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) throws Exception {
        String companyCode = callInIvrActionDTO.getCompanyCode();
        String outlineNumber = callInIvrActionDTO.getOutlineNumber();
        String uuid = callInIvrActionDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        String oriUuid = getOriUuid(uuid);

        CallBridgeDTO callBridgeDTO = CallBridgeDTO.build(callUuidContext, callInIvrActionDTO, oriUuid, outlineNumber,
                OutLineEnum.OUT_LINE);
        CompanyInfo companyInfoDTO = commonDataOperateService.getCompanyInfoDTO(companyCode);
        String mainDisplayNumber = companyInfoDTO.getMainDisplayNumber();
        callBridgeDTO.setDisplayNumber(mainDisplayNumber);
        // 保存context
        writeCallUuidContext(callInIvrActionDTO, callUuidContext, callBridgeDTO);
        log.info("[呼入ivr-外线] 转接外线: {}, 企业: {}, 来电号码: {}",
                outlineNumber, companyCode, callInIvrActionDTO.getCallerNumber());
        CallQueueToAgentDTO callQueueToAgentDTO = CallQueueToAgentDTO.build(IdUtil.fastUUID(), companyCode, uuid, outlineNumber);
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
     * 外线uuid上下文携入redis
     *
     * @param callInIvrActionDTO 呼入ivr参数
     * @param callUuidContext    来电uuid上下文-客户
     * @param callBridgeDTO      外呼并桥接参数
     */
    private void writeCallUuidContext(CallInIvrActionDTO callInIvrActionDTO,
                                      CallUuidContext callUuidContext,
                                      CallBridgeDTO callBridgeDTO) {
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setUuid(callBridgeDTO.getOriUuid());
        callUuidRelationDTO.setMainCallId(callUuidContext.getMainCallId());
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setHangupAll(true);
        callUuidRelationDTO.setRelationUuid(callUuidContext.getUUID());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.CALL_IN_IVR_TRANS_CLIENT);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.CLIENT);
        callUuidRelationDTO.setNumber(callBridgeDTO.getCalleeNumber());
        // 呼入属性
        callUuidRelationDTO.setVideo(callUuidContext.getVideo());
        callUuidRelationDTO.setAudio(callUuidContext.getAudio());
        callUuidRelationDTO.setReqId(callBridgeDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(callBridgeDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(callBridgeDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(callBridgeDTO.getCalleeNumber());
        callUuidRelationDTO.setCompanyCode(callInIvrActionDTO.getCompanyCode());
        callUuidRelationDTO.setServerId(callUuidContext.getCurrent().getServerId());
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.INBOUND);
        callUuidRelationDTO.setOs(callUuidContext.getCurrent().getOs());
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callCdrDTO.setUuid(callBridgeDTO.getOriUuid());
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        // 转接外线
        CallUuidContext outlineCallUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relationUuid(Sets.newHashSet(callBridgeDTO.getSUuid()))
                .build();
        commonDataOperateService.saveCallUuidContext(outlineCallUuidContext);

        // 呼入客户
        callUuidContext.fillRelateUuidDtoByCallBridge(callBridgeDTO.getOriUuid());
        callUuidContext.fillRelationUuidSet(callBridgeDTO.getOriUuid());
        callUuidContext.getCurrent().setRelationUuid(callBridgeDTO.getOriUuid());
        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }
}
