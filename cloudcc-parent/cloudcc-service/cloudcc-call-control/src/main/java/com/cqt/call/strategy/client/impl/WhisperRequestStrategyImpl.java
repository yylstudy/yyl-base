package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 耳语
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhisperRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.whisper;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientAdminXferDTO clientAdminXferDTO = convert(requestBody, ClientAdminXferDTO.class);
        String companyCode = clientAdminXferDTO.getCompanyCode();
        String agentId = clientAdminXferDTO.getAgentId();
        String operatedAgentId = clientAdminXferDTO.getOperatedAgentId();
        // 判断传入agent_id是否为管理员,且被耳语坐席operate_agent_id状态是否为通话中
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAdmin(companyCode, agentId, operatedAgentId);
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "1", checkAgentAvailableVO.getMessage());
        }
        AgentStatusDTO operatedAgentStatusDTO = checkAgentAvailableVO.getOperatedAgentStatusDTO();
        String operatedAgentUuid = operatedAgentStatusDTO.getUuid();
        // 查询被耳语坐席uuid信息
        CallUuidContext operatedUuidContext = dataQueryService.getCallUuidContext(companyCode, operatedAgentUuid);
        if (Objects.isNull(operatedUuidContext)) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "状态错误, 未处于通话状态!");
        }
        if (log.isDebugEnabled()) {
            log.debug("[客户端SDK-耳语] 被耳语坐席uuid: {}, 上下文: {}",
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
        if (log.isInfoEnabled()) {
            log.info("[客户端SDK-耳语] 外呼管理员坐席: {}, 参数: {}", oriUuid, objectMapper.writeValueAsString(originateDTO));
        }
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        if (log.isInfoEnabled()) {
            log.info("[客户端SDK-耳语] 外呼管理员坐席: {}, 结果: {}", oriUuid, objectMapper.writeValueAsString(freeswitchApiVO));
        }
        if (freeswitchApiVO.getResult()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, oriUuid, "0", "发起耳语成功!");
        }
        return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "发起耳语失败!");
    }

    /**
     * 保存uuid上下文
     *
     * @param clientAdminXferDTO    耳语请求参数
     * @param checkAgentAvailableVO 校验坐席结果
     * @param operatedUuidContext   被操作坐席uuid上下文
     * @param originateDTO          外呼管理员坐席参数
     */
    private void writeCallUuidContext(ClientAdminXferDTO clientAdminXferDTO,
                                      CheckAgentAvailableVO checkAgentAvailableVO,
                                      CallUuidContext operatedUuidContext,
                                      OriginateDTO originateDTO) {
        CallUuidRelationDTO current = operatedUuidContext.getCurrent();
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setMainCallId(current.getMainCallId());
        callUuidRelationDTO.setRelationUuid(operatedUuidContext.getCurrent().getUuid());
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainUuid(current.getUuid());
        callUuidRelationDTO.setXferUUID(current.getUuid());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.WHISPER_AGENT);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setCallDirectionEnum(operatedUuidContext.getCurrent().getCallDirectionEnum());
        callUuidRelationDTO.setExtId(clientAdminXferDTO.getExtId());
        callUuidRelationDTO.setExtIp(checkAgentAvailableVO.getExtStatusDTO().getExtIp());
        callUuidRelationDTO.setNumber(clientAdminXferDTO.getExtId());
        callUuidRelationDTO.setAgentId(clientAdminXferDTO.getAgentId());
        callUuidRelationDTO.setCompanyCode(clientAdminXferDTO.getCompanyCode());
        callUuidRelationDTO.setReqId(clientAdminXferDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setServerId(current.getServerId());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.XFER);
        callUuidRelationDTO.setXferActionEnum(XferActionEnum.WHISPER);
        callUuidRelationDTO.setOs(checkAgentAvailableVO.getOs());
        callUuidRelationDTO.setAudio(originateDTO.getAudio());
        callUuidRelationDTO.setVideo(originateDTO.getVideo());
        callUuidRelationDTO.setHangupAll(false);
        callUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .build();
        // 发起耳语的坐席
        dataStoreService.saveCallUuidContext(callUuidContext);

        // 被耳语坐席
        operatedUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(operatedUuidContext);
    }
    
}
