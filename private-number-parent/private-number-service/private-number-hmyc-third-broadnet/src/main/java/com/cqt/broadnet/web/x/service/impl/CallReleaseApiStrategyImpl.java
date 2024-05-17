package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.broadnet.common.constants.ApiMethodConstant;
import com.cqt.broadnet.common.model.x.dto.CallReleaseDTO;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.web.x.service.ApiStrategy;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.broadnet.web.x.service.release.CallBillStorePushService;
import com.cqt.broadnet.web.x.service.release.SmsBillStorePushService;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:05
 * 供应商推送通话结束事件实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallReleaseApiStrategyImpl implements ApiStrategy {

    private final CallBillStorePushService callBillStorePushService;

    private final SmsBillStorePushService smsBillStorePushService;

    private final ObjectMapper objectMapper;

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    @Override
    public String getMethod() {
        return ApiMethodConstant.CALL_RELEASE;
    }

    @Override
    public CallControlResponseVO execute(String jsonStr) throws JsonProcessingException {
        CallReleaseDTO callReleaseDTO = objectMapper.readValue(jsonStr, CallReleaseDTO.class);
        CallReleaseDTO.EndCallRequest endCallRequest = callReleaseDTO.convertJson(objectMapper);

        PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(endCallRequest.getSecretNo());
        // 根据callId或smsId 走不同逻辑
        // 通话
        if (StrUtil.isNotEmpty(endCallRequest.getCallId())) {
            callBillStorePushService.storeCallBill(endCallRequest, privateCorpBusinessInfoDTO);
            return CallControlResponseVO.ok();
        }

        // 短信
        if (StrUtil.isNotEmpty(endCallRequest.getSmsId())) {
            smsBillStorePushService.storeSmsBill(endCallRequest, privateCorpBusinessInfoDTO);
        }

        return CallControlResponseVO.ok();
    }

}
