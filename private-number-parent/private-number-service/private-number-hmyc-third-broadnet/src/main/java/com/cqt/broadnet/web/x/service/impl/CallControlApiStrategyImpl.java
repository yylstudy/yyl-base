package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.broadnet.common.constants.ApiMethodConstant;
import com.cqt.broadnet.common.model.x.dto.CallControlDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.web.x.service.ApiStrategy;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:05
 * 呼转控制接口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallControlApiStrategyImpl implements ApiStrategy {

    private final ObjectMapper objectMapper;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final RedissonUtil redissonUtil;

    @Override
    public String getMethod() {
        return ApiMethodConstant.CALL_CONTROL;
    }

    @Override
    public CallControlResponseVO execute(String jsonStr) throws JsonProcessingException {

        CallControlDTO callControlDTO = objectMapper.readValue(jsonStr, CallControlDTO.class);
        CallControlDTO.StartCallRequest startCallRequest = callControlDTO.convertJson(objectMapper);
        String callId = startCallRequest.getCallId();
        // 调通用号码隐藏平台查询绑定关系
        BindInfoApiQuery apiQuery = getBindInfoApiQuery(startCallRequest);
        try (HttpResponse httpResponse = HttpRequest
                .post(privateNumberBindProperties.getGetBindInfoUrl())
                .body(objectMapper.writeValueAsString(apiQuery))
                .timeout(privateNumberBindProperties.getQueryTimeout())
                .execute()) {
            String body = httpResponse.body();
            log.info("callId: {}, 通用查询绑定关系结果: {}", callId, body);

            if (!httpResponse.isOk()) {
                return CallControlResponseVO.fail("内部查询绑定关系失败!");
            }
            ResultVO<BindInfoApiVO> bindInfoResultVO = objectMapper.readValue(body, new TypeReference<ResultVO<BindInfoApiVO>>() {
            });
            if (!bindInfoResultVO.success()) {
                // code != 0
                return CallControlResponseVO.ok(bindInfoResultVO.getMessage());
            }

            // 广电 callId和平台绑定id做对应
            setRelation(callId, bindInfoResultVO);
            // 构造响应参数
            return CallControlResponseVO.buildCallControlResponseVO(apiQuery.getCaller(), bindInfoResultVO);
        }
    }

    private void setRelation(String callId, ResultVO<BindInfoApiVO> bindInfoResultVO) {
        try {
            BindInfoApiVO bindInfoApiVO = bindInfoResultVO.getData();
            String broadNetCallIdWithBindIdKey = PrivateCacheUtil.getBroadNetCallIdWithBindIdKey(callId);
            redissonUtil.setString(broadNetCallIdWithBindIdKey,
                    objectMapper.writeValueAsString(bindInfoApiVO),
                    3,
                    TimeUnit.HOURS);
        } catch (Exception e) {
            log.info("设置呼叫id: {}, 和绑定关系: {},对应关系异常: {}", callId, bindInfoResultVO, e);
        }
    }

    private BindInfoApiQuery getBindInfoApiQuery(CallControlDTO.StartCallRequest startCallRequest) {
        BindInfoApiQuery apiQuery = new BindInfoApiQuery();
        apiQuery.setCaller(startCallRequest.getCallNo());
        apiQuery.setCalled(startCallRequest.getSecretNo());
        apiQuery.setCallId(startCallRequest.getCallId());
        apiQuery.setDigitInfo(startCallRequest.getExtension());
        apiQuery.setBehaviorType(startCallRequest.getRecordType());
        return apiQuery;
    }
}
