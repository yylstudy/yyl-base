package com.cqt.broadnet.web.axb.service;

import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.broadnet.web.x.service.release.AbstractStorePushService;
import com.cqt.broadnet.web.x.service.release.StatusBillStorePushService;
import com.cqt.broadnet.web.x.service.retry.CallBillPushRetryImpl;
import com.cqt.common.constants.RabbitMqConstant;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.common.util.AuthUtil;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateStatusInfo;
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
 * date:  2023-05-26 14:06
 * AXB 话单入库和推送客户
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxbCallBillStorePushService extends AbstractStorePushService {

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final CallBillPushRetryImpl callBillPushRetryImpl;

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    private final StatusBillStorePushService statusBillStorePushService;

    /**
     * 通话话单处理入口
     *
     * @param privateBillInfo 推送客户接口报文
     * @param callstat        话单入库报文
     */
    @Async("saveExecutor")
    public void storeCallBill(PrivateBillInfo privateBillInfo, Callstat callstat) {
        if (privateBillInfo == null || callstat == null) {
            return;
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(privateBillInfo.getTelX());
        String vccId = businessInfoDTO.getVccId();

        // 发mq入库
        rabbitTemplate.convertAndSend(RabbitMqConstant.ICCP_CDR_SAVE_EXCHANGE, RabbitMqConstant.ICCP_CDR_SAVE_ROUTE_KEY, callstat);
        log.info("callId: {}, vccId: {}, AXB通话话单推送mq入库finish", privateBillInfo.getRecordId(), vccId);
        // 设置ts和sign
        setSign(privateBillInfo, businessInfoDTO);
        // 推送接口
        pushToCustomer(privateBillInfo.getRecordId(),
                privateBillInfo,
                vccId,
                businessInfoDTO.getCdrPushFlag(),
                businessInfoDTO.getBillPushUrl(),
                businessInfoDTO.getPushRetryNum(),
                Duration.ofMinutes(businessInfoDTO.getPushRetryMin())
        );

        // 挂断事件 hangup
        PrivateStatusInfo privateStatusInfo = buildStatusInfo(privateBillInfo);
        statusBillStorePushService.setSign(privateStatusInfo,businessInfoDTO);
        pushToCustomer(privateBillInfo.getRecordId(),
                privateStatusInfo,
                vccId,
                businessInfoDTO.getStatusPushFlag(),
                businessInfoDTO.getStatusPushUrl(),
                businessInfoDTO.getPushRetryNum(),
                Duration.ofMinutes(businessInfoDTO.getPushRetryMin()));
    }

    private PrivateStatusInfo buildStatusInfo(PrivateBillInfo privateBillInfo) {
        PrivateStatusInfo statusInfo = new PrivateStatusInfo();
        statusInfo.setEvent(CallEventEnum.hangup.name());
        statusInfo.setRecordId(privateBillInfo.getRecordId());
        // 未提供, 自己存
        statusInfo.setBindId(privateBillInfo.getBindId());
        statusInfo.setCaller(privateBillInfo.getTelA());
        statusInfo.setCalled(privateBillInfo.getTelB());
        statusInfo.setTelX(privateBillInfo.getTelX());
        statusInfo.setCurrentTime(privateBillInfo.getReleaseTime());
        // 需转化
        statusInfo.setCallResult(privateBillInfo.getCallResult());
        statusInfo.setExt(privateBillInfo.getExt());
        return statusInfo;
    }

    private void setSign(PrivateBillInfo privateBillInfo, PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO) {
        privateBillInfo.setTs(System.currentTimeMillis());
        TreeMap<String, Object> treeMap = objectMapper.convertValue(privateBillInfo, new TypeReference<TreeMap<String, Object>>() {
        });
        String sign = AuthUtil.createSign(treeMap, privateCorpBusinessInfoDTO.getVccId(), privateCorpBusinessInfoDTO.getSecretKey());
        privateBillInfo.setSign(sign);
    }

    @Override
    public void pushDataToMq(PushRetryDataDTO pushRetryDataDTO) {
        callBillPushRetryImpl.pushDataToMq(pushRetryDataDTO);
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
        return "AXB通话话单";
    }
}
