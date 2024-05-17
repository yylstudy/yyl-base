package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.date.DateUtil;
import com.cqt.broadnet.common.constants.ApiMethodConstant;
import com.cqt.broadnet.common.model.x.dto.SmsInterceptDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.web.x.service.ApiStrategy;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.common.constants.RabbitMqConstant;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.sms.save.SmsRequest;
import com.cqt.model.sms.save.SmsRequestBody;
import com.cqt.model.sms.save.SmsRequestHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:05
 * AXB短信托收推送接口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsInterceptApiStrategyImpl implements ApiStrategy {

    private final ObjectMapper objectMapper;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public String getMethod() {
        return ApiMethodConstant.SMS_INTERCEPT;
    }

    @Override
    public CallControlResponseVO execute(String jsonStr) throws JsonProcessingException {
        SmsInterceptDTO smsInterceptDTO = objectMapper.readValue(jsonStr, SmsInterceptDTO.class);
        SmsInterceptDTO.SmsInterceptRequest smsInterceptRequest = smsInterceptDTO.convertJson(objectMapper);

        PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = privateCorpBusinessInfoService
                .getPrivateCorpBusinessInfoDTO(smsInterceptRequest.getSecretNo());
        String vccId = privateCorpBusinessInfoDTO.getVccId();
        // 构造
        SmsRequest smsRequest = buildSmsRequest(smsInterceptRequest, vccId);
        // 发mq入库
        rabbitTemplate.convertAndSend(RabbitMqConstant.ICCP_SMS_CDR_SAVE_EXCHANGE, RabbitMqConstant.ICCP_SMS_CDR_SAVE_ROUTE_KEY, smsRequest);
        log.info("smsId: {}, vccId: {}, 短信托收推送mq入库finish", smsInterceptRequest.getCallId(), vccId);

        return CallControlResponseVO.ok();
    }

    private SmsRequest buildSmsRequest(SmsInterceptDTO.SmsInterceptRequest smsInterceptRequest, String vccId) {
        SmsRequest sendSmsRequest = new SmsRequest();
        SmsRequestBody body = new SmsRequestBody();
        SmsRequestHeader header = new SmsRequestHeader();
        header.setStreamNumber(smsInterceptRequest.getCallId());
        header.setMessageId(smsInterceptRequest.getCallId());
        body.setBindId(smsInterceptRequest.getSubsId());
        body.setAPhoneNumber(smsInterceptRequest.getCallNo());
        body.setBPhoneNumber("");
        body.setInPhoneNumber(smsInterceptRequest.getSecretNo());
        body.setOutPhoneNumber(smsInterceptRequest.getSecretNo());
        body.setVccId(vccId);
        body.setRequestTime(DateUtil.formatTime(smsInterceptRequest.getMtTime()));
        // TODO 31：正常--未规定
        body.setFailCode("99");
        body.setFailReason("托收");
        body.setCallerNumber(smsInterceptRequest.getCallNo());
        body.setSupplierId(privateNumberBindProperties.getSupplierId());
        // 短信内容，请使用UCS2进行编码
        // 取值样例：30104E2D
        body.setInContent(decodeUcs2(smsInterceptRequest.getSmsContent()));
        sendSmsRequest.setHeader(header);
        sendSmsRequest.setBody(body);
        return sendSmsRequest;
    }

    private String decodeUcs2(String src) {
        byte[] bytes = new byte[src.length() / 2];
        for (int i = 0; i < src.length(); i += 2) {
            bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
        }
        return new String(bytes, StandardCharsets.UTF_16BE);
    }
}
