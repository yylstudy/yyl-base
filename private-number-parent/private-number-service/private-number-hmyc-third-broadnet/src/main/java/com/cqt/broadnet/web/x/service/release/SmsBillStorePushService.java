package com.cqt.broadnet.web.x.service.release;

import cn.hutool.core.util.StrUtil;
import com.cqt.broadnet.common.model.x.dto.CallReleaseDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.x.service.retry.SmsBillPushRetryImpl;
import com.cqt.common.constants.RabbitMqConstant;
import com.cqt.common.util.AuthUtil;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.sms.dto.CommonSmsBillPushDTO;
import com.cqt.model.sms.save.SmsRequest;
import com.cqt.model.sms.save.SmsRequestBody;
import com.cqt.model.sms.save.SmsRequestHeader;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date:  2023-02-16 14:50
 * 短信话单入库推送处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsBillStorePushService extends AbstractStorePushService {

    private final com.cqt.broadnet.web.x.service.TelCodeService TelCodeService;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final SmsBillPushRetryImpl smsBillPushRetryImpl;

    /**
     * 短信话单处理入口
     */
    @Async("saveExecutor")
    public void storeSmsBill(CallReleaseDTO.EndCallRequest endCallRequest,
                             PrivateCorpBusinessInfoDTO businessInfoDTO) {
        String vccId = businessInfoDTO.getVccId();
        // 构造
        SmsRequest smsRequest = buildSmsRequest(endCallRequest, vccId);
        // 发mq入库
        rabbitTemplate.convertAndSend(RabbitMqConstant.ICCP_SMS_CDR_SAVE_EXCHANGE, RabbitMqConstant.ICCP_SMS_CDR_SAVE_ROUTE_KEY, smsRequest);
        log.info("smsId: {}, vccId: {}, 短信话单推送mq入库finish", endCallRequest.getSmsId(), vccId);

        // 推送接口
        CommonSmsBillPushDTO commonSmsBillPushDTO = buildCommonSmsBillPushDTO(endCallRequest);

        // 设置ts和sign
        setSign(commonSmsBillPushDTO, businessInfoDTO);
        pushToCustomer(commonSmsBillPushDTO.getSmsId(),
                commonSmsBillPushDTO,
                vccId,
                businessInfoDTO.getCdrPushFlag(),
                businessInfoDTO.getBillPushUrl(),
                businessInfoDTO.getPushRetryNum(),
                Duration.ofMinutes(businessInfoDTO.getPushRetryMin()));

    }

    private void setSign(CommonSmsBillPushDTO commonSmsBillPushDTO, PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO) {
        commonSmsBillPushDTO.setTs(System.currentTimeMillis());
        TreeMap<String, Object> treeMap = objectMapper.convertValue(commonSmsBillPushDTO, new TypeReference<TreeMap<String, Object>>() {
        });
        String sign = AuthUtil.createSign(treeMap, privateCorpBusinessInfoDTO.getVccId(), privateCorpBusinessInfoDTO.getSecretKey());
        commonSmsBillPushDTO.setSign(sign);
    }

    /**
     * 短信话单入库, 发送mq消息
     */
    private SmsRequest buildSmsRequest(CallReleaseDTO.EndCallRequest endCallRequest, String vccId) {
        //获取 失败码
        Integer releaseCause = endCallRequest.getReleaseCause();
        SmsRequest sendSmsRequest = new SmsRequest();
        SmsRequestBody body = new SmsRequestBody();
        SmsRequestHeader header = new SmsRequestHeader();
        header.setStreamNumber(endCallRequest.getSmsId());
        header.setMessageId(endCallRequest.getSmsId());
        // TODO 未提供
        body.setBindId("");
        body.setAPhoneNumber(endCallRequest.getCallerNum());
        body.setBPhoneNumber(endCallRequest.getCalleeNum());
        body.setInPhoneNumber(endCallRequest.getSecretNo());
        body.setOutPhoneNumber(endCallRequest.getSecretNo());
        body.setVccId(vccId);
        body.setRequestTime(endCallRequest.getStartTime());
        body.setSendTime(endCallRequest.getStartTime());
        // TODO 31：正常--未规定
        body.setFailCode(releaseCause == 31 ? "0" : "99");
        body.setFailReason(releaseCause == 31 ? "成功" : "其他错误");
        body.setCallerNumber(endCallRequest.getCallerNum());
        body.setCalledNumber(endCallRequest.getCalleeNum());
        body.setSmsNumber(endCallRequest.getSmsNumber());
        body.setSupplierId(privateNumberBindProperties.getSupplierId());
        // TODO 未提供
        body.setInContent(endCallRequest.getSmsContent());
        sendSmsRequest.setHeader(header);
        sendSmsRequest.setBody(body);
        return sendSmsRequest;
    }


    /**
     * 构造短信话单推送数据
     */
    private CommonSmsBillPushDTO buildCommonSmsBillPushDTO(CallReleaseDTO.EndCallRequest endCallRequest) {
        CommonSmsBillPushDTO commonSmsBillPushDTO = new CommonSmsBillPushDTO();
        commonSmsBillPushDTO.setAreaCode(TelCodeService.getAreaCode(StrUtil.subPre(endCallRequest.getSecretNo(), 7)));
        // TODO 未提供
        commonSmsBillPushDTO.setBindId("");
        commonSmsBillPushDTO.setReceiver(endCallRequest.getCalleeNum());
        commonSmsBillPushDTO.setReceiverShow(endCallRequest.getSecretNo());
        commonSmsBillPushDTO.setSender(endCallRequest.getCallerNum());
        commonSmsBillPushDTO.setSenderShow(endCallRequest.getSecretNo());
        commonSmsBillPushDTO.setSmsContent(endCallRequest.getSmsContent());
        commonSmsBillPushDTO.setSmsId(endCallRequest.getSmsId());
        commonSmsBillPushDTO.setSmsNumber(endCallRequest.getSmsNumber());
        // TODO 31：正常--未规定
        commonSmsBillPushDTO.setSmsResult(endCallRequest.getReleaseCause() == 31 ? "0" : "99");
        commonSmsBillPushDTO.setTransferTime(endCallRequest.getStartTime());
        return commonSmsBillPushDTO;
    }

    @Override
    public void pushDataToMq(PushRetryDataDTO pushRetryDataDTO) {
        smsBillPushRetryImpl.pushDataToMq(pushRetryDataDTO);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public Integer getPushTimeout() {
        return privateNumberBindProperties.getPushTimeout();
    }

    @Override
    public String type() {
        return "短信话单";
    }
}
