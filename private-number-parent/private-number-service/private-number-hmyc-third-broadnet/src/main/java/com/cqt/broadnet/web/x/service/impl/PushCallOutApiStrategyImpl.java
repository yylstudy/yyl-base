package com.cqt.broadnet.web.x.service.impl;

import com.cqt.broadnet.common.constants.ApiMethodConstant;
import com.cqt.broadnet.common.model.x.dto.PushCallOutDTO;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.web.x.service.ApiStrategy;
import com.cqt.broadnet.web.x.service.release.StatusBillStorePushService;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-04-11 13:48
 * 供应商推送呼出事件接口API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushCallOutApiStrategyImpl implements ApiStrategy {

    private final ObjectMapper objectMapper;

    private final StatusBillStorePushService statusBillStorePushService;

    @Override
    public String getMethod() {
        return ApiMethodConstant.PUSH_CALL_OUT;
    }

    @Override
    public CallControlResponseVO execute(String jsonStr) throws JsonProcessingException {
        PushCallOutDTO pushCallOutDTO = objectMapper.readValue(jsonStr, PushCallOutDTO.class);
        PushCallOutDTO.RequestBody requestBody = pushCallOutDTO.convertJson(objectMapper);

        // 构造参数 @see com.cqt.model.push.entity.PrivateStatusInfo
        PrivateStatusInfo statusInfo = pushCallOutDTO.buildStatusInfo(requestBody);

        // 推送接口
        statusBillStorePushService.pushStatus(statusInfo);

        return CallControlResponseVO.ok();
    }
}
