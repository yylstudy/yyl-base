package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientAdminXferDTO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 代接
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubstituteRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.substitute;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientAdminXferDTO clientAdminXferDTO = convert(requestBody, ClientAdminXferDTO.class);
        String companyCode = clientAdminXferDTO.getCompanyCode();
        String agentId = clientAdminXferDTO.getAgentId();
        String operatedAgentId = clientAdminXferDTO.getOperatedAgentId();
        // 判断传入agent_id是否为管理员,且被代接坐席operate_agent_id状态是否为通话中
        // TODO 需要判断被代接坐席是否在转接, 咨询, 三方通话
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAdmin(companyCode, agentId, operatedAgentId);
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "1", checkAgentAvailableVO.getMessage());
        }
        AgentStatusDTO operatedAgentStatusDTO = checkAgentAvailableVO.getOperatedAgentStatusDTO();
        String operatedAgentUuid = operatedAgentStatusDTO.getUuid();
        // 查询被代接坐席uuid信息
        CallUuidContext operatedUuidContext = dataQueryService.getCallUuidContext(companyCode, operatedAgentUuid);
        if (log.isInfoEnabled()) {
            log.info("[客户端SDK-代接] 被代接坐席uuid: {}, 上下文: {}",
                    operatedAgentUuid, objectMapper.writeValueAsString(operatedUuidContext));
        }
        CallUuidRelationDTO current = operatedUuidContext.getCurrent();
        String agentDisplayNumber = dataQueryService.getAgentDisplayNumber(checkAgentAvailableVO.getCompanyInfo(),
                checkAgentAvailableVO.getAgentInfo());
        OriginateDTO originateDTO = new OriginateDTO();
        String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setReqId(clientAdminXferDTO.getReqId());
        originateDTO.setOriUuid(oriUuid);
        originateDTO.setCompanyCode(companyCode);
        originateDTO.setCallerNumber(agentDisplayNumber);
        originateDTO.setDisplayNumber(agentDisplayNumber);
        originateDTO.setCalleeNumber(clientAdminXferDTO.getExtId());
        originateDTO.setOutLine(OutLineEnum.IN_LINE.getCode());
        originateDTO.setServerId(current.getServerId());
        originateDTO.setAudio(current.getAudio());
        originateDTO.setVideo(current.getVideo());
        // 设置通道变量
        originateDTO.setOriUserData(operatedUuidContext.getCurrent().getMainUuid());
        // 先写入redis
        writeCallUuidContext(clientAdminXferDTO, checkAgentAvailableVO, operatedUuidContext, originateDTO);
        log.info("[客户端SDK-代接] 企业id: {}, 管理员坐席: {}, 被代接坐席: {}", companyCode, agentId, operatedAgentId);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        if (freeswitchApiVO.getResult()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, oriUuid, "0", "代接成功!");
        }
        return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "代接失败!");
    }

    /**
     * uuid上下文
     *
     * @param clientAdminXferDTO    代接请求参数
     * @param checkAgentAvailableVO 坐席检测结果
     * @param operatedUuidContext   被操作坐席uuid上下文
     * @param originateDTO          外呼管理员参数
     */
    private void writeCallUuidContext(ClientAdminXferDTO clientAdminXferDTO,
                                      CheckAgentAvailableVO checkAgentAvailableVO,
                                      CallUuidContext operatedUuidContext,
                                      OriginateDTO originateDTO) {
        CallUuidRelationDTO current = operatedUuidContext.getCurrent();
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        // 管理员坐席uuid
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        // 关联客户uuid
        callUuidRelationDTO.setRelationUuid(current.getRelationUuid());
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setMainUuid(current.getUuid());
        callUuidRelationDTO.setMainCallId(current.getMainCallId());
        // 呼入属性
        callUuidRelationDTO.setCallInFlag(true);
        callUuidRelationDTO.setCallInChannel(CallInChannelEnum.SUBSTITUTE);
        // 坐席号码-分机id
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.SUBSTITUTE_AGENT);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setCallDirectionEnum(operatedUuidContext.getCurrent().getCallDirectionEnum());
        callUuidRelationDTO.setNumber(clientAdminXferDTO.getExtId());
        callUuidRelationDTO.setExtId(checkAgentAvailableVO.getExtStatusDTO().getExtId());
        callUuidRelationDTO.setExtIp(checkAgentAvailableVO.getExtStatusDTO().getExtIp());
        callUuidRelationDTO.setAgentId(clientAdminXferDTO.getAgentId());
        callUuidRelationDTO.setReqId(clientAdminXferDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setCompanyCode(clientAdminXferDTO.getCompanyCode());
        callUuidRelationDTO.setServerId(current.getServerId());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.SUBSTITUTE);
        callUuidRelationDTO.setOs(checkAgentAvailableVO.getOs());
        callUuidRelationDTO.setHangupAll(true);
        callUuidRelationDTO.setAudio(originateDTO.getAudio());
        callUuidRelationDTO.setVideo(originateDTO.getVideo());
        callUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relationUuid(Sets.newHashSet(current.getRelationUuid()))
                .build();
        // 管理员坐席uuid
        dataStoreService.saveCallUuidContext(callUuidContext);

        // 被代接坐席
        operatedUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        operatedUuidContext.getCurrent().setHangupAll(false);
        operatedUuidContext.getCurrent().getCallCdrDTO().setHangupCauseEnum(HangupCauseEnum.SUBSTITUTE);
        operatedUuidContext.fillRelateUuidDtoBySubstitute(originateDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(operatedUuidContext);

        // 客户uuid
        CallUuidContext clientUuidContext = dataQueryService.getCallUuidContext(current.getCompanyCode(),
                current.getRelationUuid());
        clientUuidContext.getCurrent().setRelationUuid(originateDTO.getOriUuid());
        clientUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(clientUuidContext);

    }

}
