package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.agent.vo.RelateUuidDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 外呼任务请求处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundCallTaskClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final DataQueryService dataQueryService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.call_task;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        final ClientOutboundCallTaskDTO clientOutboundCallTaskDTO = convert(requestBody, ClientOutboundCallTaskDTO.class);
        // 调用底层originate接口
        OriginateDTO originateDTO = OriginateDTO.task2Originate(clientOutboundCallTaskDTO);
        String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
        originateDTO.setOriUuid(oriUuid);
        // 先写入redis
        writeCallUuidContext(clientOutboundCallTaskDTO, originateDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
        if (freeswitchApiVO.getResult()) {
            return ClientResponseBaseVO.response(clientOutboundCallTaskDTO, oriUuid, "0", "发起呼叫客户成功!");
        }
        return ClientResponseBaseVO.response(clientOutboundCallTaskDTO, "1", "发起呼叫客户失败!");
    }

    private void writeCallUuidContext(ClientOutboundCallTaskDTO clientOutboundCallTaskDTO, OriginateDTO originateDTO) {

        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        // 主话单id-自定义
        callUuidRelationDTO.setMainCallId(dataQueryService.createMainCallId());
        callUuidRelationDTO.setMainCdrFlag(true);
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setNumber(clientOutboundCallTaskDTO.getCalleeNumber());
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.CLIENT_CALLER);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.CLIENT);
        callUuidRelationDTO.setExtId(null);
        callUuidRelationDTO.setExtIp(null);
        callUuidRelationDTO.setReqId(clientOutboundCallTaskDTO.getReqId());
        callUuidRelationDTO.setOs(null);
        callUuidRelationDTO.setCallerNumber(clientOutboundCallTaskDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(clientOutboundCallTaskDTO.getDisplayNumber());
        callUuidRelationDTO.setPlatformNumber(clientOutboundCallTaskDTO.getPlatformNumber());
        // 客户号码
        callUuidRelationDTO.setCalleeNumber(clientOutboundCallTaskDTO.getCalleeNumber());
        callUuidRelationDTO.setCompanyCode(clientOutboundCallTaskDTO.getCompanyCode());
        OriginateAfterActionEnum action = OriginateAfterActionEnum.valueOf(clientOutboundCallTaskDTO.getTaskType());
        callUuidRelationDTO.setOriginateAfterActionEnum(action);
        callUuidRelationDTO.setOutCallTaskFlag(true);
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.OUTBOUND);
        callUuidRelationDTO.setAudio(clientOutboundCallTaskDTO.getAudio());
        callUuidRelationDTO.setVideo(clientOutboundCallTaskDTO.getVideo());
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_A);
        // 外呼请求参数
        callUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relateUuidDTO(new RelateUuidDTO())
                .clientOutboundCallTaskDTO(clientOutboundCallTaskDTO)
                .build();
        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }

}
