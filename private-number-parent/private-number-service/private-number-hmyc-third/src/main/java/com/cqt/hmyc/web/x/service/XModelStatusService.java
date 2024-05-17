package com.cqt.hmyc.web.x.service;

import cn.hutool.core.util.StrUtil;
import com.cqt.cloud.api.push.BindPushFeignClient;
import com.cqt.common.util.ThirdUtils;
import com.cqt.hmyc.web.corpinfo.service.IPrivateCorpBusinessInfoService;
import com.cqt.hmyc.web.x.model.XModelStatusDTO;
import com.cqt.model.common.Result;
import com.cqt.model.common.ThirdPushResult;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-06-07 14:59
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XModelStatusService {

    private final BindPushFeignClient bindPushFeignClient;

    private final ObjectMapper objectMapper;

    private final IPrivateCorpBusinessInfoService iPrivateCorpBusinessInfoService;

    private final RedissonUtil redissonUtil;

    public ThirdPushResult status(XModelStatusDTO xmodelStatusDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("和多号, x模式状态报文: {}", objectMapper.writeValueAsString(xmodelStatusDTO));
        }
        String vccId = iPrivateCorpBusinessInfoService.getVccId(ThirdUtils.getNumberUn86(xmodelStatusDTO.getSecretNo()));
        if (StrUtil.isEmpty(vccId)) {
            return ThirdPushResult.fail("1001", "X号码不在本平台!");
        }
        String bindId = redissonUtil.getString("bindId_" + xmodelStatusDTO.getBindId());
        PrivateStatusInfo statusInfo = xmodelStatusDTO.buildPrivateStatusInfo(vccId);
        if (StringUtils.isNotEmpty(bindId)){
            statusInfo.setBindId(bindId);
        }
        if (log.isInfoEnabled()) {
            log.info("内部x模式状态报文: {}", objectMapper.writeValueAsString(statusInfo));
        }

        // 保证成功
        Result result = bindPushFeignClient.statusReceiver(statusInfo);
        log.info("callId: {}, 和多号, x模式状态推送客户结果: {}", xmodelStatusDTO.getCallId(), objectMapper.writeValueAsString(result));
        if (result.getCode() != 0) {
            // 重试
            return ThirdPushResult.fail("1001", "处理失败!");
        }
        return ThirdPushResult.ok();
    }

}
