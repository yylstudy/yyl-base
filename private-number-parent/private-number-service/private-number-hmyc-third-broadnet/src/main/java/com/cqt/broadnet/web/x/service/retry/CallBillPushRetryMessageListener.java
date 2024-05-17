package com.cqt.broadnet.web.x.service.retry;

import com.cqt.broadnet.common.cache.CorpBusinessConfigCache;
import com.cqt.common.util.AuthUtil;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.rabbitmq.dynamic.AbstractDynamicMessageListener;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-02-16 11:03
 * 通话话单推送接口失败重试, 消息监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallBillPushRetryMessageListener extends AbstractDynamicMessageListener {

    private final CallBillPushRetryImpl callBillPushRetryImpl;

    private final ObjectMapper objectMapper;

    @Override
    public void doMessage(String body) {
        log.info("消费通话话单重推数据: {}", body);
        try {
            PushRetryDataDTO pushRetryDataDTO = objectMapper.readValue(body, PushRetryDataDTO.class);

            // 重新设置ts和sign
            Optional<PrivateCorpBusinessInfoDTO> dtoOptional = CorpBusinessConfigCache.get(pushRetryDataDTO.getVccId());
            dtoOptional.ifPresent(dto -> pushRetryDataDTO.setPushData(AuthUtil.resetSign(objectMapper, dto, pushRetryDataDTO.getPushData())));

            callBillPushRetryImpl.retry(pushRetryDataDTO);
        } catch (JsonProcessingException e) {
            log.error("doMessage data: {},  JsonProcessing error: ", body, e);
        }
    }

    @Override
    public void errorCatch(String body, Exception e) {

    }

    @Override
    public String getContainerName() {

        return callBillPushRetryImpl.getDeadQueueName();
    }
}
