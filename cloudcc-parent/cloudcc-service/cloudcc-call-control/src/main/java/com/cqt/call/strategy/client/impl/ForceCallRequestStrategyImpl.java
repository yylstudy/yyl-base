package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientAdminXferDTO;
import com.cqt.model.client.vo.ClientForceCallVO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 2、强插：加入通话，形成三方通话；
 * （1）强插者话务条状态
 * 　　a、话务条处于通话状态，
 * 　　b、展示强插状态：【强插通话：号码和号码】；
 * 　　c、话务条功能区：挂断、按键、更多、设置；
 * （2）被强插者无法发起转接、咨询、三方通话，按钮处于禁用状态；
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForceCallRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.force_call;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientAdminXferDTO clientAdminXferDTO = convert(requestBody, ClientAdminXferDTO.class);
        String companyCode = clientAdminXferDTO.getCompanyCode();
        String agentId = clientAdminXferDTO.getAgentId();
        String operatedAgentId = clientAdminXferDTO.getOperatedAgentId();
        // 判断传入agent_id是否为管理员,且被强插坐席operate_agent_id状态是否为通话中
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAdmin(companyCode, agentId, operatedAgentId);
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, SdkErrCode.AGENT_INVALID.getCode(),
                    checkAgentAvailableVO.getMessage());
        }
        String uuid = checkAgentAvailableVO.getOperatedAgentStatusDTO().getUuid();
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "状态错误, 未处于通话状态!");
        }
        CallUuidRelationDTO current = callUuidContext.getCurrent();

        // 外呼管理员
        OriginateDTO originateDTO = new OriginateDTO();
        String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setReqId(clientAdminXferDTO.getReqId());
        originateDTO.setOriUuid(oriUuid);
        originateDTO.setCompanyCode(companyCode);
        String agentDisplayNumber = dataQueryService.getAgentDisplayNumber(checkAgentAvailableVO.getCompanyInfo(),
                checkAgentAvailableVO.getAgentInfo());
        originateDTO.setCallerNumber(agentDisplayNumber);
        originateDTO.setDisplayNumber(agentDisplayNumber);
        // 内线为分机id
        originateDTO.setCalleeNumber(clientAdminXferDTO.getExtId());
        originateDTO.setOutLine(OutLineEnum.IN_LINE.getCode());
        originateDTO.setServerId(current.getServerId());
        originateDTO.setAudio(current.getAudio());
        originateDTO.setVideo(current.getVideo());
        // 先写入redis
        Set<String> inCallNumbers = writeCallUuidContext(clientAdminXferDTO, checkAgentAvailableVO, callUuidContext,
                originateDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        log.info("[SDK-强插] 企业: {}, 坐席: {}, 外呼内线: {}, inCallNumbers: {}, 发起外呼完成",
                companyCode, agentId, operatedAgentId, inCallNumbers);
        if (freeswitchApiVO.getResult()) {
            return ClientForceCallVO.response(clientAdminXferDTO, oriUuid, inCallNumbers, SdkErrCode.START_FORCE_CALL_OK);
        }
        return ClientResponseBaseVO.response(clientAdminXferDTO, SdkErrCode.START_FORCE_CALL_FAIL);
    }

    private Set<String> writeCallUuidContext(ClientAdminXferDTO clientAdminXferDTO,
                                             CheckAgentAvailableVO checkAgentAvailableVO,
                                             CallUuidContext agentUuidContext,
                                             OriginateDTO originateDTO) {
        String companyCode = clientAdminXferDTO.getCompanyCode();
        CallUuidRelationDTO agentCurrent = agentUuidContext.getCurrent();

        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainCallId(agentCurrent.getMainCallId());
        callUuidRelationDTO.setMainUuid(agentCurrent.getMainUuid());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.FORCE_CALL_AGENT);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setCallDirectionEnum(agentUuidContext.getCurrent().getCallDirectionEnum());
        callUuidRelationDTO.setXferUUID(agentUuidContext.getCurrent().getUuid());
        callUuidRelationDTO.setRelationUuid(agentCurrent.getRelationUuid());
        callUuidRelationDTO.setNumber(clientAdminXferDTO.getExtId());
        callUuidRelationDTO.setExtId(clientAdminXferDTO.getExtId());
        callUuidRelationDTO.setExtIp(checkAgentAvailableVO.getAgentStatusDTO().getExtIp());
        callUuidRelationDTO.setAgentId(clientAdminXferDTO.getAgentId());
        callUuidRelationDTO.setCompanyCode(companyCode);
        callUuidRelationDTO.setReqId(clientAdminXferDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setServerId(agentCurrent.getServerId());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.XFER);
        callUuidRelationDTO.setXferActionEnum(XferActionEnum.FORCE_CALL);
        callUuidRelationDTO.setAudio(originateDTO.getAudio());
        callUuidRelationDTO.setVideo(originateDTO.getVideo());
        callUuidRelationDTO.setOs(agentUuidContext.getCurrent().getOs());
        callUuidRelationDTO.setThreeWay(true);
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        CallUuidContext joinThreeWayCallUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relationUuid(Sets.newHashSet(agentCurrent.getUuid(), agentCurrent.getRelationUuid()))
                .build();
        // 接入三方通话人员
        dataStoreService.saveCallUuidContext(joinThreeWayCallUuidContext);

        // 发起三方通话坐席
        agentUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        agentUuidContext.getCurrent().setThreeWay(true);
        dataStoreService.saveCallUuidContext(agentUuidContext);

        // 客户
        CallUuidContext clientCallUuidContext = dataQueryService.getCallUuidContext(companyCode,
                agentCurrent.getRelationUuid());
        clientCallUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        clientCallUuidContext.getCurrent().setThreeWay(true);
        dataStoreService.saveCallUuidContext(clientCallUuidContext);

        return Sets.newHashSet(clientCallUuidContext.getCurrent().getNumber(),
                joinThreeWayCallUuidContext.getCurrent().getNumber());
    }

}
