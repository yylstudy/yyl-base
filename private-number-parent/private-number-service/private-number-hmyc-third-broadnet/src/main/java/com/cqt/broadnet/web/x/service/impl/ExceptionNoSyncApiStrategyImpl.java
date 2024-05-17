package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.broadnet.common.constants.ApiMethodConstant;
import com.cqt.broadnet.common.model.x.dto.ExceptionNoSyncDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.web.x.service.ApiStrategy;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.numpool.dto.CommonExceptionNoSyncDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:05
 * 异常号码状态同步接口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionNoSyncApiStrategyImpl implements ApiStrategy {

    private final ObjectMapper objectMapper;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    @Override
    public String getMethod() {
        return ApiMethodConstant.EXCEPTION_NO_SYNC;
    }

    private static final Map<Integer, Integer> STATE_MAP = new ConcurrentHashMap<>();

    static {
        STATE_MAP.put(0, 1);
        STATE_MAP.put(1, 2);
    }

    @Override
    public CallControlResponseVO execute(String jsonStr) throws JsonProcessingException {

        ExceptionNoSyncDTO exceptionNoSyncDTO = objectMapper.readValue(jsonStr, ExceptionNoSyncDTO.class);

        // 查询这个x号码所属企业, 推送异常号码url
        PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(exceptionNoSyncDTO.getSecretNo());
        if (StrUtil.isEmpty(privateCorpBusinessInfoDTO.getExceptionNoSyncUrl())) {
            return CallControlResponseVO.ok();
        }
        // 推送客户接口, 不重推!
        CommonExceptionNoSyncDTO noSyncDTO = CommonExceptionNoSyncDTO.builder()
                .number(exceptionNoSyncDTO.getSecretNo())
                .state(STATE_MAP.get(exceptionNoSyncDTO.getStatus()))
                .reason(exceptionNoSyncDTO.getExceptionMsg())
                .build();
        String exceptionNoSyncUrl = privateCorpBusinessInfoDTO.getExceptionNoSyncUrl();
        String data = objectMapper.writeValueAsString(noSyncDTO);
        try (HttpResponse httpResponse = HttpRequest.post(exceptionNoSyncUrl)
                .timeout(privateNumberBindProperties.getPushTimeout())
                .body(data)
                .executeAsync()) {
            log.info("号码异常推送客户接口: {}, 数据: {}, 结果: {}", exceptionNoSyncUrl, data, httpResponse.body());
        } catch (Exception e) {
            log.error("号码异常推送客户异常, 接口: {}, 数据: {}, 结果: {}", exceptionNoSyncUrl, data, e);
        }

        return CallControlResponseVO.ok();
    }
}
