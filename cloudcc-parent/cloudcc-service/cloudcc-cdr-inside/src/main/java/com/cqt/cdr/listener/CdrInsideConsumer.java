package com.cqt.cdr.listener;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.cdr.entity.AcrRecord;
import com.cqt.cdr.service.*;
import com.cqt.cdr.util.AccessRemote;
import com.cqt.cdr.util.CommonUtils;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.dto.InsideCdrMessageDTO;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
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
//@Component
//@RequiredArgsConstructor
//@RocketMQMessageListener(topic = RocketMqConstant.CDR_INSIDE_TOPIC, consumerGroup = RocketMqConstant.APP_NAME)
public class CdrInsideConsumer implements RocketMQListener<InsideCdrMessageDTO> {
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    IMainCdrService mainCdrService;
    @Resource
    ICdrChanneldataService cdrChanneldataService;
    @Resource
    ISubCdrService subCdrService;
    @Resource
    private LeaveMessageService leaveMessageService;
    @Resource
    AcrRecordService acrRecordService;


    @Override
    public void onMessage(InsideCdrMessageDTO cdrMessageDTO) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 内部话单消费 | ";
        // 处理消息逻辑
        processMessage(cdrMessageDTO, LOG_TAG);
    }

    public void processMessage(InsideCdrMessageDTO cdrMessageDTO, String LOG_TAG) {
        String callId = cdrMessageDTO.getMainCdr().getCallId();
        String companyCode = cdrMessageDTO.getMainCdr().getCompanyCode();
        List<CallCenterSubCdr> subCdrs = cdrMessageDTO.getSubCdr() != null ? cdrMessageDTO.getSubCdr() : new ArrayList<>();

        CallCenterMainCdr mainCdr = cdrMessageDTO.getMainCdr();
        CdrChanneldata cdrChanneldata = cdrMessageDTO.getChannelData();
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
        } catch (Exception e) {
            log.error(LOG_TAG + "消息: {}, 消费异常: {}", cdrMessageDTO, e);
            Message<InsideCdrMessageDTO> insideCdrMsg = MessageBuilder.withPayload(cdrMessageDTO).setHeader(RocketMQHeaders.KEYS, callId).build();
            rocketMQTemplate.syncSend(getRetryDestination(companyCode), insideCdrMsg, 5000l, 16);
        } finally {
            RequestDataHelper.remove();
        }
    }

    private String getRetryDestination(String companyCode) {
        return RocketMqConstant.CDR_INSIDE_TOPIC + StrUtil.COLON + companyCode;
    }
}