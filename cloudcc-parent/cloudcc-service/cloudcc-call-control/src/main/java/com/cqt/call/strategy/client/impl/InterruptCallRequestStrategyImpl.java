package com.cqt.call.strategy.client.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientAdminXferDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 强拆
 * 管理员强制拆除结束该通话，被强制结束通话的坐席收到提示：当前通话已被管理员强制结束；
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterruptCallRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.interrupt_call;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientAdminXferDTO clientAdminXferDTO = convert(requestBody, ClientAdminXferDTO.class);
        String companyCode = clientAdminXferDTO.getCompanyCode();
        String agentId = clientAdminXferDTO.getAgentId();
        String operatedAgentId = clientAdminXferDTO.getOperatedAgentId();
        // 判断传入agent_id是否为管理员,且被强拆坐席operate_agent_id状态是否为通话中
        Optional<AgentStatusDTO> statusOptional = dataQueryService.getActualAgentStatus(companyCode, operatedAgentId);
        if (!statusOptional.isPresent()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "400", StrFormatter.format("坐席: {}, 未签入!", agentId));
        }
        AgentStatusDTO agentStatusDTO = statusOptional.get();
        if (!AgentStatusEnum.CALLING.name().equals(agentStatusDTO.getTargetStatus())
                && !AgentStatusEnum.RINGING.name().equals(agentStatusDTO.getTargetStatus())) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "坐席未处于振铃中或通话中状态, 强拆失败!");
        }
        String operatedAgentUuid = agentStatusDTO.getUuid();
        // 查询被强拆坐席uuid信息
        CallUuidContext operatedUuidContext = dataQueryService.getCallUuidContext(companyCode, operatedAgentUuid);
        if (Objects.isNull(operatedUuidContext)) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "状态错误, 未处于通话状态!");
        }
        if (log.isDebugEnabled()) {
            log.debug("[客户端SDK-强拆] 被强拆坐席uuid: {}, 上下文: {}",
                    operatedAgentUuid, objectMapper.writeValueAsString(operatedUuidContext));
        }
        //
        writeCallUuidContext(operatedUuidContext);
        // 挂断坐席
        CallUuidRelationDTO operatedCurrent = operatedUuidContext.getCurrent();
        String operatedCurrentUuid = operatedCurrent.getUuid();
        log.info("[强拆] 企业id: {}, 管理员坐席: {}, 被操作坐席: {}, 挂断uuid: {}", companyCode, agentId, operatedAgentId, operatedCurrentUuid);
        HangupDTO hangupDTO = HangupDTO.build(companyCode, true, operatedCurrentUuid, HangupCauseEnum.INTERRUPT_CALL);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.hangup(hangupDTO);
        // 挂断关联坐席
        Set<String> relationUuid = operatedUuidContext.getRelationUuid();
        log.info("[强拆] 企业id: {}, 管理员坐席: {}, 被操作坐席: {}, 关联uuid: {}", companyCode, agentId, operatedAgentId, relationUuid);
        if (CollUtil.isNotEmpty(relationUuid)) {
            for (String id : relationUuid) {
                HangupDTO hangup = HangupDTO.build(companyCode, false, id, HangupCauseEnum.INTERRUPT_CALL);
                freeswitchRequestService.hangup(hangup);
                log.info("[强拆] 企业id: {}, 管理员坐席: {}, 被操作坐席: {}, 挂断关联uuid: {}", companyCode, agentId, operatedAgentId, id);
            }
        }
        if (freeswitchApiVO.getResult()) {
            return ClientResponseBaseVO.response(clientAdminXferDTO, "0", "强拆成功!");
        }
        return ClientResponseBaseVO.response(clientAdminXferDTO, "1", "强拆失败!");
    }

    private void writeCallUuidContext(CallUuidContext operatedUuidContext) throws Exception {
        CallUuidRelationDTO current = operatedUuidContext.getCurrent();
        current.getCallCdrDTO().setHangupCauseEnum(HangupCauseEnum.INTERRUPT_CALL);
        dataStoreService.saveCallUuidContext(operatedUuidContext);
    }

}
