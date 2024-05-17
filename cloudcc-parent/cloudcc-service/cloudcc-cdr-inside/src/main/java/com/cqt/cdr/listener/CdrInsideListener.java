package com.cqt.cdr.listener;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.base.contants.CommonConstant;
import com.cqt.cdr.entity.AcrRecord;
import com.cqt.cdr.service.*;
import com.cqt.cdr.util.AccessRemote;
import com.cqt.cdr.util.CommonUtils;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author xinson
 * date:  2023-08-09 19:14
 * 内部话单生成
 */
@Slf4j
@Component
//@RequiredArgsConstructor
//@RocketMQMessageListener(topic = "cloudcc_cdr_store_topic", consumerGroup = "cloudcc_insidecdr-group")
@Deprecated
public class CdrInsideListener implements RocketMQListener<String> {
    private static final Logger CDR_RESOLVE_EXCEPTION_LOG = LoggerFactory.getLogger("cdrResolveExceptionLogger");
    @Resource
    IMainCdrService mainCdrService;
    @Resource
    ICdrChanneldataService cdrChanneldataService;
    @Resource
    ISubCdrService subCdrService;
    @Resource
    AcrRecordService acrRecordService;
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    private AccessRemote accessRemote;
    @Resource
    private LeaveMessageService leaveMessageService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(String message) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 内部话单消费 | ";
        log.info(LOG_TAG + "接收到MQ消息: " + message);
        // 处理消息逻辑
        processMessage(message, LOG_TAG);
    }

    public void processMessage(String message, String LOG_TAG) {
        try {
            CdrMessageDTO cdrMessageDTO = CdrMessageDTO.getCdrMessageDTO(message, objectMapper, LOG_TAG);
            if (cdrMessageDTO == null) {
                log.warn(LOG_TAG + "json解析异常，消息：{}，存入日志文件", message);
                CDR_RESOLVE_EXCEPTION_LOG.info(message);
                return;
            }
            String callId = cdrMessageDTO.getMainCdr().getCallId();
            String companyCode = cdrMessageDTO.getMainCdr().getCompanyCode();
            List<CallCenterSubCdr> subCdrs = cdrMessageDTO.getSubCdr() != null ? cdrMessageDTO.getSubCdr() : new ArrayList<>();
            for (CallCenterSubCdr subCdr : subCdrs) {
                if (!accessRemote.getStoragePath(cdrMessageDTO.getServiceId(), subCdr, LOG_TAG)) {
                    log.warn(LOG_TAG + "从底层获取录音路径异常，消息：{}，存入异常队列", message);
                    Message<String> msg = MessageBuilder.withPayload(message).setHeader(RocketMQHeaders.KEYS, callId).build();
                    rocketMQTemplate.syncSend("cloudcc_insidecdr_error_topic" + StrUtil.COLON + companyCode, msg, 2000, 6);
                    return;
                }
            }

            Message<String> msg = MessageBuilder.withPayload(JSONObject.toJSONString(cdrMessageDTO)).setHeader(RocketMQHeaders.KEYS, callId).build();
            rocketMQTemplate.syncSend("cloudcc_cdr_outside_topic" + StrUtil.COLON + companyCode, msg);
            CallCenterMainCdr mainCdr = cdrMessageDTO.getMainCdr();
            CdrChanneldata cdrChanneldata = cdrMessageDTO.getCdrChanneldata();
            String month = DateUtil.format(mainCdr.getHangupTime(), CommonConstant.MONTH_FORMAT);
            try {
                RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, companyCode);
                RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
                ArrayList<AcrRecord> acrRecords = new ArrayList<>();
                for (CallCenterSubCdr subCdr : subCdrs) {
                    AcrRecord acrRecord = acrRecordService.getAcrRecord(mainCdr, subCdr, cdrChanneldata);
                    if (acrRecord != null) {
                        acrRecords.add(acrRecord);
                    }
                    subCdrService.insertSubCdr(LOG_TAG, subCdrs, companyCode, month, subCdr);
                }
                // 主话单录入
                mainCdrService.inserMainCdr(LOG_TAG, mainCdr, companyCode, month);
                if (cdrMessageDTO.getVoiceMailFlag() != null && cdrMessageDTO.getVoiceMailFlag()) {
                    leaveMessageService.insertLeaveMessage(LOG_TAG, mainCdr, companyCode, month);
                }
                // 通道变量录入
                if (cdrChanneldata != null) {
                    cdrChanneldataService.insertCdrChannel(LOG_TAG, mainCdr, cdrChanneldata, companyCode, month);
                }
                // 计费号码录入
                for (AcrRecord acrRecord : acrRecords) {
                    acrRecord.setCalledpartynumber(CommonUtils.replaceNumPerfix(acrRecord.getCalledpartynumber()));
                    acrRecord.setCallingpartynumber(CommonUtils.replaceNumPerfix(acrRecord.getCallingpartynumber()));
                    acrRecord.setSpecificchargedpar(CommonUtils.replaceNumPerfix(acrRecord.getSpecificchargedpar()));
                    acrRecord.setTranslatednumber(CommonUtils.replaceNumPerfix(acrRecord.getTranslatednumber()));
                    acrRecord.setOricallingnumber(CommonUtils.replaceNumPerfix(acrRecord.getOricallingnumber()));
                    acrRecord.setOricallednumber(CommonUtils.replaceNumPerfix(acrRecord.getOricallednumber()));
                    acrRecordService.insertAcrRecord(LOG_TAG, month, acrRecords, acrRecord);
                }
            } catch (RedisConnectionException redisConnectionException) {
                Message<String> msg2 = MessageBuilder.withPayload(message).setHeader(RocketMQHeaders.KEYS, callId).build();
                rocketMQTemplate.syncSend("cloudcc_insidecdr_error_topic" + StrUtil.COLON + companyCode, msg2, 2000, 3);
            } finally {
                RequestDataHelper.remove();
            }
            log.info(LOG_TAG + "消息:{}，消息消费成功", message);
        } catch (Exception e) {
            log.error(LOG_TAG + "消息：{}，处理异常，存入日志文件", message, e);
            CDR_RESOLVE_EXCEPTION_LOG.info("消息：{}，异常信息：", message, e);
        }
    }
}
