package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.util.FreeswitchUtil;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientHangupDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 挂断
 * uuid     string     是     需要挂断的通话uuid
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HangupClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.hangup;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientHangupDTO clientHangupDTO = convert(requestBody, ClientHangupDTO.class);
        String companyCode = clientHangupDTO.getCompanyCode();
        String uuid = clientHangupDTO.getUuid();
        if (StrUtil.isEmpty(uuid)) {
            log.info("[SDK-挂断请求] 企业id: {}, uuid: {}, 为空!", companyCode, uuid);
            return ClientResponseBaseVO.response(clientHangupDTO, "1", "uuid为空!");
        }
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            log.info("[SDK-挂断请求] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return ClientResponseBaseVO.response(clientHangupDTO, SdkErrCode.UUID_NOT_FIND);
        }
        if (log.isDebugEnabled()) {
            log.debug("[客户端SDK-挂断] 当前uuid上下文: {}", objectMapper.writeValueAsString(callUuidContext));
        }
        callUuidContext.getCurrent().getCallCdrDTO().setHangupCauseEnum(getHangupCause(callUuidContext));
        dataStoreService.saveCallUuidContext(callUuidContext);

        HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.AGENT_HANGUP);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.hangup(hangupDTO);
        if (freeswitchApiVO.getResult()) {
            return ClientResponseBaseVO.response(clientHangupDTO, SdkErrCode.OK.getCode(), "挂断成功!");
        }
        Integer code = freeswitchApiVO.getCode();
        if (Objects.nonNull(code) && FreeswitchUtil.isInvalidUuid(freeswitchApiVO.getMsg())) {
            return ClientResponseBaseVO.response(clientHangupDTO, SdkErrCode.OK.getCode(), "当前通话已挂断!");
        }
        return ClientResponseBaseVO.response(clientHangupDTO, SdkErrCode.HANGUP_FAIL);
    }

    private HangupCauseEnum getHangupCause(CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCurrent().getCallCdrDTO();
        // 没有接通事件
        if (!Boolean.TRUE.equals(callCdrDTO.getAnswerFlag())) {
            return HangupCauseEnum.AGENT_REJECT;
        }
        CallDirectionEnum callDirectionEnum = callUuidContext.getCurrent().getCallDirectionEnum();
        // 呼入-被叫
        if (CallDirectionEnum.INBOUND.equals(callDirectionEnum)) {
            return HangupCauseEnum.CALLEE_RELEASE;
        }
        // 呼出-主叫
        return HangupCauseEnum.CALLER_RELEASE;
    }
}
