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
import com.cqt.model.client.dto.ClientPlayRecordDTO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
import com.cqt.model.freeswitch.dto.api.StopPlayDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 话务条播放录音
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayRecordRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.play_record;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        final ClientPlayRecordDTO clientPlayRecordDTO = convert(requestBody, ClientPlayRecordDTO.class);
        String companyCode = clientPlayRecordDTO.getCompanyCode();
        String agentId = clientPlayRecordDTO.getAgentId();
        String extId = clientPlayRecordDTO.getExtId();
        String uuid = clientPlayRecordDTO.getUuid();

        // 存在uuid 直接播放录音
        if (StrUtil.isNotEmpty(uuid)) {
            // 先结束放音
            StopPlayDTO stopPlayDTO = StopPlayDTO.build(companyCode, uuid);
            freeswitchRequestService.stopPlay(stopPlayDTO);
            // 还在通话中直接播放录音
            PlaybackDTO playbackDTO = PlaybackDTO.build(companyCode, uuid, clientPlayRecordDTO.getFilePath(), 1);
            FreeswitchApiVO apiVO = freeswitchRequestService.playback(playbackDTO);
            if (BooleanUtils.isTrue(apiVO.getResult())) {
                return ClientResponseBaseVO.response(clientPlayRecordDTO, uuid, "0", "录音播放成功!");
            }
            return ClientResponseBaseVO.response(clientPlayRecordDTO, "1", "录音播放失败!");
        }

        // 提取公共校验坐席模板方法
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                getAgentStatusEnumList(),
                companyCode, extId, agentId);
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientPlayRecordDTO, "1", checkAgentAvailableVO.getMessage());
        }

        // 调用底层originate接口
        String displayNumber = getDisplayNumber(checkAgentAvailableVO);
        OriginateDTO originateDTO = OriginateDTO.clientPlayRecord2Originate(clientPlayRecordDTO, displayNumber);
        String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setOriUuid(oriUuid);
        // 先写入redis
        writeCallUuidContext(clientPlayRecordDTO, checkAgentAvailableVO, originateDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        if (freeswitchApiVO.getResult()) {
            dataStoreService.cancelArrangeTask(companyCode, agentId);
            return ClientResponseBaseVO.response(clientPlayRecordDTO, oriUuid, "0", "发起外呼成功, 等待坐席接通!");
        }
        return ClientResponseBaseVO.response(clientPlayRecordDTO, "1", "发起外呼失败!");
    }

    private ArrayList<AgentStatusEnum> getAgentStatusEnumList() {
        return Lists.newArrayList(AgentStatusEnum.FREE,
                AgentStatusEnum.BUSY,
                AgentStatusEnum.REST,
                AgentStatusEnum.ARRANGE,
                AgentStatusEnum.CALLING);
    }

    private void writeCallUuidContext(ClientPlayRecordDTO clientPlayRecordDTO,
                                      CheckAgentAvailableVO checkAgentAvailableVO,
                                      OriginateDTO originateDTO) {
        ExtStatusDTO extStatusDTO = checkAgentAvailableVO.getExtStatusDTO();
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        // 主话单id-自定义
        callUuidRelationDTO.setMainCallId(dataQueryService.createMainCallId());
        callUuidRelationDTO.setMainCdrFlag(true);
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setNumber(clientPlayRecordDTO.getExtId());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.AGENT_CALLER);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        callUuidRelationDTO.setExtId(clientPlayRecordDTO.getExtId());
        callUuidRelationDTO.setExtIp(extStatusDTO.getExtIp());
        callUuidRelationDTO.setReqId(clientPlayRecordDTO.getReqId());
        callUuidRelationDTO.setOs(checkAgentAvailableVO.getOs());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setPlatformNumber(originateDTO.getCallerNumber());
        // 客户号码
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setAgentId(clientPlayRecordDTO.getAgentId());
        callUuidRelationDTO.setCompanyCode(clientPlayRecordDTO.getCompanyCode());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.PLAY_RECORD);
        callUuidRelationDTO.setPlayRecordPath(clientPlayRecordDTO.getFilePath());
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.OUTBOUND);
        callUuidRelationDTO.setAudio(clientPlayRecordDTO.getAudio());
        callUuidRelationDTO.setVideo(clientPlayRecordDTO.getVideo());
        callUuidRelationDTO.setHangupAfterPlayback(true);
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relateUuidDTO(new RelateUuidDTO())
                .build();
        commonDataOperateService.saveCallUuidContext(callUuidContext);

    }

}
