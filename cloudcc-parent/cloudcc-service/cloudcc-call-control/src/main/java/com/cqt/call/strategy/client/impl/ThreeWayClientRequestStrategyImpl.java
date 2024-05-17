package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientThreeWayDTO;
import com.cqt.model.client.vo.ClientThreeWayVO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 发起三方通话
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreeWayClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.three_way;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientThreeWayDTO clientThreeWayDTO = convert(requestBody, ClientThreeWayDTO.class);
        String companyCode = clientThreeWayDTO.getCompanyCode();
        String agentId = clientThreeWayDTO.getAgentId();
        log.info("[三方通话] 企业: {}, 坐席: {}", companyCode, agentId);
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.CALLING,
                Lists.newArrayList(AgentStatusEnum.CALLING),
                companyCode, clientThreeWayDTO.getExtId(), agentId, getOutlineNumber(clientThreeWayDTO));
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientThreeWayDTO, SdkErrCode.AGENT_INVALID.getCode(),
                    checkAgentAvailableVO.getMessage());
        }
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, clientThreeWayDTO.getUuid());

        // 外呼人员
        OriginateDTO originateDTO = new OriginateDTO();
        String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setReqId(clientThreeWayDTO.getReqId());
        originateDTO.setOriUuid(oriUuid);
        originateDTO.setCompanyCode(companyCode);
        originateDTO.setCallerNumber(clientThreeWayDTO.getExtId());
        originateDTO.initThreeWayProperties(clientThreeWayDTO);
        // 查企业主显号码-打外线时显示
        String displayNumber = dataQueryService.getAgentDisplayNumber(checkAgentAvailableVO.getCompanyInfo(),
                checkAgentAvailableVO.getAgentInfo());
        // ThreeWayTypeNumber 内线为坐席id、
        String calleeNumber = clientThreeWayDTO.getThreeWayNumber();
        CheckAgentAvailableVO agentCheckAgentAvailableVO = null;
        if (OutLineEnum.IN_LINE.getCode().equals(clientThreeWayDTO.getThreeWayType())) {
            // 校验三方通过人员为坐席, 是否可用
            agentCheckAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                    Lists.newArrayList(AgentStatusEnum.FREE),
                    companyCode, "", calleeNumber);
            if (!agentCheckAgentAvailableVO.getAvailable()) {
                return ClientResponseBaseVO.response(clientThreeWayDTO, SdkErrCode.AGENT_INVALID.getCode(),
                        agentCheckAgentAvailableVO.getMessage());
            }
            displayNumber = clientThreeWayDTO.getExtId();
            calleeNumber = agentCheckAgentAvailableVO.getAgentInfo().getSysExtId();
            log.info("[三方通话] 企业: {}, 坐席: {}, 外呼内线: {}", companyCode, agentId, calleeNumber);
        }
        originateDTO.setDisplayNumber(displayNumber);
        originateDTO.setCalleeNumber(calleeNumber);
        originateDTO.setOutLine(clientThreeWayDTO.getThreeWayType());
        originateDTO.setServerId(callUuidContext.getCurrent().getServerId());
        originateDTO.initThreeWayProperties(clientThreeWayDTO);

        // 先写入redis
        Set<String> inCallNumbers = writeCallUuidContext(clientThreeWayDTO, callUuidContext, originateDTO,
                agentCheckAgentAvailableVO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        log.info("[三方通话] 企业: {}, 坐席: {}, 外呼内线: {}, 发起外呼完成", companyCode, agentId, calleeNumber);
        if (freeswitchApiVO.getResult()) {
            return ClientThreeWayVO.response(clientThreeWayDTO, inCallNumbers, SdkErrCode.START_THREE_WAY_OK);
        }
        return ClientResponseBaseVO.response(clientThreeWayDTO, SdkErrCode.START_THREE_WAY_FAIL);
    }

    private String getOutlineNumber(ClientThreeWayDTO clientThreeWayDTO) {
        if (OutLineEnum.OUT_LINE.getCode().equals(clientThreeWayDTO.getThreeWayType())) {
            return clientThreeWayDTO.getThreeWayNumber();
        }
        return null;
    }

    /**
     * 通话uuid上下文
     *
     * @param clientThreeWayDTO          三方通话请求入参
     * @param agentUuidContext           发起三方通话坐席uuid上下文
     * @param originateDTO               外呼接入三方通话人员入参
     * @param agentCheckAgentAvailableVO 接入三方通话人员为坐席的校验信息
     */
    private Set<String> writeCallUuidContext(ClientThreeWayDTO clientThreeWayDTO,
                                             CallUuidContext agentUuidContext,
                                             OriginateDTO originateDTO,
                                             CheckAgentAvailableVO agentCheckAgentAvailableVO) {
        String companyCode = clientThreeWayDTO.getCompanyCode();
        CallUuidRelationDTO agentCurrent = agentUuidContext.getCurrent();
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        CallTypeEnum callType = CallTypeEnum.AGENT;
        CallRoleEnum callRole = CallRoleEnum.THREE_WAY_AGENT;
        if (OutLineEnum.OUT_LINE.getCode().equals(clientThreeWayDTO.getThreeWayType())) {
            callType = CallTypeEnum.CLIENT;
            callRole = CallRoleEnum.THREE_WAY_CLIENT;
        } else {
            // 呼入属性
            callUuidRelationDTO.setVideo(clientThreeWayDTO.getVideo());
            callUuidRelationDTO.setAudio(clientThreeWayDTO.getAudio());
            callUuidRelationDTO.setCallInFlag(true);
            callUuidRelationDTO.setCallInChannel(CallInChannelEnum.THREE_WAY);
        }
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainUuid(agentCurrent.getUuid());
        callUuidRelationDTO.setXferUUID(agentCurrent.getUuid());
        // 关联uuid-发起三方通话的坐席uuid
        callUuidRelationDTO.setRelationUuid(agentCurrent.getUuid());
        callUuidRelationDTO.setMainCallId(agentCurrent.getMainCallId());
        callUuidRelationDTO.setCallRoleEnum(callRole);
        callUuidRelationDTO.setCallTypeEnum(callType);
        callUuidRelationDTO.setNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setCompanyCode(companyCode);
        if (Objects.nonNull(agentCheckAgentAvailableVO)) {
            callUuidRelationDTO.setExtId(agentCheckAgentAvailableVO.getExtStatusDTO().getExtId());
            callUuidRelationDTO.setExtIp(agentCheckAgentAvailableVO.getExtStatusDTO().getExtIp());
            callUuidRelationDTO.setAgentId(agentCheckAgentAvailableVO.getAgentInfo().getSysAgentId());
        }
        callUuidRelationDTO.setReqId(clientThreeWayDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setServerId(agentCurrent.getServerId());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.XFER);
        callUuidRelationDTO.setXferActionEnum(XferActionEnum.THREE_WAY);
        callUuidRelationDTO.setCallDirectionEnum(agentUuidContext.getCurrent().getCallDirectionEnum());
        callUuidRelationDTO.setOs(agentCurrent.getOs());
        callUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        callUuidRelationDTO.setThreeWay(true);
        callUuidRelationDTO.setAudio(originateDTO.getAudio());
        callUuidRelationDTO.setVideo(originateDTO.getVideo());
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        CallUuidContext joinThreeWayCallUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relationUuid(Sets.newHashSet(agentCurrent.getUuid(), agentCurrent.getRelationUuid()))
                .build();
        // 接入三方通话人员
        dataStoreService.saveCallUuidContext(joinThreeWayCallUuidContext);

        // 与发起三方通话坐席原通话人员
        CallUuidContext bridgeCallUuidContext = dataQueryService.getCallUuidContext(companyCode, agentCurrent.getRelationUuid());
        bridgeCallUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        bridgeCallUuidContext.getCurrent().setThreeWay(true);
        bridgeCallUuidContext.fillRelateUuidDtoByThreeWay(originateDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(bridgeCallUuidContext);

        // 发起三方通话坐席
        agentUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        agentUuidContext.getCurrent().setThreeWay(true);
        agentUuidContext.fillRelateUuidDtoByThreeWay(originateDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(agentUuidContext);

        return Sets.newHashSet(bridgeCallUuidContext.getCurrent().getNumber(),
                joinThreeWayCallUuidContext.getCurrent().getNumber());
    }

}
