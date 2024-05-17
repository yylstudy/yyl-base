package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.BooleanUtil;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.agent.AgentCallingSubStatusEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientHoldDTO;
import com.cqt.model.client.vo.ClientHoldVO;
import com.cqt.model.freeswitch.dto.api.HoldDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 呼叫保持
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoldClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.hold;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientHoldDTO clientHoldDTO = convert(requestBody, ClientHoldDTO.class);
        HoldDTO holdDTO = new HoldDTO();
        holdDTO.setReqId(clientHoldDTO.getReqId());
        holdDTO.setCompanyCode(clientHoldDTO.getCompanyCode());
        // TODO 保持音怎么查?
        holdDTO.setUuid(clientHoldDTO.getUuid());
        boolean hold = BooleanUtil.toBoolean(clientHoldDTO.getHold());
        holdDTO.setValue(hold);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.hold(holdDTO);
        Boolean result = freeswitchApiVO.getResult();
        if (Boolean.TRUE.equals(result)) {
            // 调用SDK-Interface
            //  1. 坐席状态迁移 坐席状态: 通话中, 子状态: 保持中
            //  2. 保存坐席实时状态
            //  3. 通知前端SDK
            AgentStatusTransferBO transferBO = AgentStatusTransferBO.builder()
                    .companyCode(clientHoldDTO.getCompanyCode())
                    .os(clientHoldDTO.getOs())
                    .uuid(clientHoldDTO.getUuid())
                    .extId(clientHoldDTO.getExtId())
                    .agentId(clientHoldDTO.getAgentId())
                    .eventTimestamp(System.currentTimeMillis())
                    .targetStatus(AgentStatusEnum.CALLING)
                    .targetSubStatus(hold ? AgentCallingSubStatusEnum.HOLD : null)
                    .transferAction(hold ? AgentStatusTransferActionEnum.HOLD : AgentStatusTransferActionEnum.UN_HOLD)
                    .build();
            boolean transfer = dataStoreService.agentStatusChangeTransfer(transferBO);
            log.info("[客户端SDK-保持] 调用SDK-Interface 坐席id: {}, 坐席状态迁移结果: {}", clientHoldDTO.getAgentId(), transfer);
            return ClientHoldVO.response(clientHoldDTO, "0", hold ? "保持成功!" : "取消保持成功!");
        }
        return ClientHoldVO.response(clientHoldDTO, "1", hold ? "保持失败!" : "取消保持失败!");
    }
}
