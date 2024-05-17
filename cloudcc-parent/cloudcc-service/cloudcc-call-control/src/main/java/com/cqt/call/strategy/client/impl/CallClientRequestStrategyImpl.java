package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.agent.vo.RelateUuidDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientCallDTO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 前端SDK 外呼请求处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.call;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        // TODO 外呼需要对坐席加锁
        final ClientCallDTO clientCallDTO = convert(requestBody, ClientCallDTO.class);
        String companyCode = clientCallDTO.getCompanyCode();
        String agentId = clientCallDTO.getAgentId();
        String extId = clientCallDTO.getExtId();

        // 提取公共校验坐席模板方法
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                Lists.newArrayList(AgentStatusEnum.FREE, AgentStatusEnum.BUSY, AgentStatusEnum.REST, AgentStatusEnum.ARRANGE),
                companyCode, extId, agentId, getOutlineNumber(clientCallDTO));
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientCallDTO, "1", checkAgentAvailableVO.getMessage());
        }
        
        // 外呼内线, 需要判断是否在在线且空闲
        if (OutLineEnum.IN_LINE.getCode().equals(clientCallDTO.getOutLine())) {
            // 内线是坐席!
            CheckAgentAvailableVO inlineAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                    Lists.newArrayList(AgentStatusEnum.FREE),
                    companyCode, "", clientCallDTO.getCalleeNumber());
            if (!inlineAgentAvailableVO.getAvailable()) {
                return ClientResponseBaseVO.response(clientCallDTO, "1", inlineAgentAvailableVO.getMessage());
            }
        }

        // 调用底层originate接口
        OriginateDTO originateDTO = OriginateDTO.clientCall2Originate(clientCallDTO);
        originateDTO.setOutLine(OutLineEnum.IN_LINE.getCode());
        String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setOriUuid(oriUuid);
        // 先写入redis
        writeCallUuidContext(clientCallDTO, checkAgentAvailableVO, originateDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        if (freeswitchApiVO.getResult()) {
            dataStoreService.cancelArrangeTask(companyCode, agentId);
            return ClientResponseBaseVO.response(clientCallDTO, oriUuid, "0", "发起外呼成功!");
        }
        return ClientResponseBaseVO.response(clientCallDTO, "1", "发起外呼失败!");
    }

    /**
     * 查询外线号码
     *
     * @param clientCallDTO 外呼参数
     * @return 外线号码
     */
    private String getOutlineNumber(ClientCallDTO clientCallDTO) {
        if (OutLineEnum.OUT_LINE.getCode().equals(clientCallDTO.getOutLine())) {
            return clientCallDTO.getCalleeNumber();
        }
        return null;
    }

    private void writeCallUuidContext(ClientCallDTO clientCallDTO,
                                      CheckAgentAvailableVO checkAgentAvailableVO,
                                      OriginateDTO originateDTO) {
        String displayNumber = getDisplayNumber(checkAgentAvailableVO);

        ExtStatusDTO extStatusDTO = checkAgentAvailableVO.getExtStatusDTO();
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        // 主话单id-自定义
        callUuidRelationDTO.setMainCallId(dataQueryService.createMainCallId());
        callUuidRelationDTO.setMainCdrFlag(true);
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setNumber(clientCallDTO.getExtId());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.AGENT_CALLER);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setExtId(clientCallDTO.getExtId());
        callUuidRelationDTO.setExtIp(extStatusDTO.getExtIp());
        callUuidRelationDTO.setReqId(clientCallDTO.getReqId());
        callUuidRelationDTO.setOs(checkAgentAvailableVO.getOs());
        callUuidRelationDTO.setCallerNumber(clientCallDTO.getExtId());
        callUuidRelationDTO.setDisplayNumber(displayNumber);
        callUuidRelationDTO.setCalleeNumber(clientCallDTO.getCalleeNumber());
        callUuidRelationDTO.setPlatformNumber(displayNumber);
        callUuidRelationDTO.setCallBridgeDisplayNumber(displayNumber);
        // 客户号码
        callUuidRelationDTO.setCalleeNumber(clientCallDTO.getCalleeNumber());
        callUuidRelationDTO.setAgentId(clientCallDTO.getAgentId());
        callUuidRelationDTO.setCompanyCode(clientCallDTO.getCompanyCode());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.CALL_BRIDGE_CLIENT);
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.OUTBOUND);
        callUuidRelationDTO.setAudio(clientCallDTO.getAudio());
        callUuidRelationDTO.setVideo(clientCallDTO.getVideo());
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_A);
        // 外呼请求参数
        callUuidRelationDTO.setClientCallDTO(clientCallDTO);
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relateUuidDTO(new RelateUuidDTO())
                .build();
        callUuidContext.fillWorkOrderId(clientCallDTO.getWorkOrderId());
        commonDataOperateService.saveCallUuidContext(callUuidContext);

        // 保存计费号码
        commonDataOperateService.saveCdrPlatformNumber(callUuidRelationDTO.getCompanyCode(),
                callUuidRelationDTO.getMainCallId(),
                displayNumber);

    }

}
