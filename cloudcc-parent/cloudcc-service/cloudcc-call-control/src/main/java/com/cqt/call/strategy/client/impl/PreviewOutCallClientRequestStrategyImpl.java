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
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
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
 * 前端SDK 预览外呼接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewOutCallClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.preview_out_call;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        // TODO 外呼需要对坐席加锁
        final ClientPreviewOutCallDTO previewOutCallDTO = convert(requestBody, ClientPreviewOutCallDTO.class);
        String companyCode = previewOutCallDTO.getCompanyCode();
        String agentId = previewOutCallDTO.getAgentId();
        String extId = previewOutCallDTO.getExtId();
        String clientNumber = previewOutCallDTO.getClientNumber();

        // 提取公共校验坐席模板方法
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                Lists.newArrayList(AgentStatusEnum.FREE, AgentStatusEnum.BUSY, AgentStatusEnum.REST, AgentStatusEnum.ARRANGE),
                companyCode, extId, agentId, clientNumber);
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(previewOutCallDTO, "1", checkAgentAvailableVO.getMessage());
        }

        // 调用底层originate接口
        OriginateDTO originateDTO = OriginateDTO.clientPreviewOutCall2Originate(previewOutCallDTO);
        String oriUuid = "preview" + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setOriUuid(oriUuid);
        // 先写入redis
        writeCallUuidContext(previewOutCallDTO, checkAgentAvailableVO, originateDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        if (freeswitchApiVO.getResult()) {
            dataStoreService.cancelArrangeTask(companyCode, agentId);
            return ClientResponseBaseVO.response(previewOutCallDTO, oriUuid, "0", "发起预览外呼成功!");
        }
        return ClientResponseBaseVO.response(previewOutCallDTO, "1", "发起预览外呼失败!");
    }

    private void writeCallUuidContext(ClientPreviewOutCallDTO previewOutCallDTO,
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
        callUuidRelationDTO.setNumber(previewOutCallDTO.getExtId());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.AGENT_CALLER);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setExtId(previewOutCallDTO.getExtId());
        callUuidRelationDTO.setExtIp(extStatusDTO.getExtIp());
        callUuidRelationDTO.setReqId(previewOutCallDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(previewOutCallDTO.getExtId());
        callUuidRelationDTO.setDisplayNumber(displayNumber);
        callUuidRelationDTO.setCallBridgeDisplayNumber(displayNumber);
        callUuidRelationDTO.setPlatformNumber(displayNumber);
        // 客户号码
        callUuidRelationDTO.setCalleeNumber(previewOutCallDTO.getClientNumber());
        callUuidRelationDTO.setAgentId(previewOutCallDTO.getAgentId());
        callUuidRelationDTO.setCompanyCode(previewOutCallDTO.getCompanyCode());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.PREVIEW_TASK);
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.OUTBOUND);
        callUuidRelationDTO.setAudio(previewOutCallDTO.getAudio());
        callUuidRelationDTO.setVideo(previewOutCallDTO.getVideo());
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_A);
        callUuidRelationDTO.setOs(checkAgentAvailableVO.getAgentStatusDTO().getOs());
        // 外呼请求参数
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relateUuidDTO(new RelateUuidDTO())
                .clientPreviewOutCallDTO(previewOutCallDTO)
                .build();
        commonDataOperateService.saveCallUuidContext(callUuidContext);

        // 保存计费号码
        commonDataOperateService.saveCdrPlatformNumber(callUuidRelationDTO.getCompanyCode(),
                callUuidRelationDTO.getMainCallId(),
                displayNumber);

    }

}
