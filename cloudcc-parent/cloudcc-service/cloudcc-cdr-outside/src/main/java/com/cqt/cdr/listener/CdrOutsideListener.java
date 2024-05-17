package com.cqt.cdr.listener;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.base.contants.CommonConstant;
import com.cqt.cdr.conf.DynamicConfig;
import com.cqt.cdr.entity.DictItem;
import com.cqt.cdr.service.DictItemService;
import com.cqt.cdr.service.PushErrService;
import com.cqt.cdr.util.AccessRemote;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrDatapushPushEntity;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
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
@RocketMQMessageListener(topic = "cloudcc_cdr_outside_topic", consumerGroup = "cloudcc_cdr_outside-group")
public class CdrOutsideListener implements RocketMQListener<String> {
    private static final Logger CDR_RESOLVE_EXCEPTION_LOG = LoggerFactory.getLogger("cdrResolveExceptionLogger");
    @Resource
    private DynamicConfig dynamicConfig;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private AccessRemote accessRemote;
    @Resource
    private PushErrService pushErrService;
    @Resource
    private DictItemService dictItemService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(String message) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 外部全量话单消费 | ";
        log.info(LOG_TAG + "接收到MQ消息: " + message);
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
            String companyCode = cdrMessageDTO.getMainCdr().getCompanyCode();
            String month = DateUtil.format(cdrMessageDTO.getMainCdr().getHangupTime(), CommonConstant.MONTH_FORMAT);
            RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
            DictItem dictItem = dictItemService.isHaveDictItem(message, LOG_TAG, month, companyCode);
            if (dictItem == null) {
                log.warn(LOG_TAG + "字典没有配置发送路径，消息：{}，存入数据库", message);
                pushErrService.errIntoErrTable(message, null, "2", month, companyCode, LOG_TAG);
                return;
            }
            List<CallCenterSubCdr> subCdrs = cdrMessageDTO.getSubCdr();
            List<String> qualityCompanys = dynamicConfig.getQualityCompanys();
            for (String qualityCompany : qualityCompanys) {
                if (!companyCode.equals(qualityCompany)) {
                    continue;
                }
                message = JSONObject.toJSONString(cdrMessageDTO);
                log.info(LOG_TAG + "企业（{}）是质检企业，消息：{}，发送到质检队列", companyCode, message);
                Message<String> msg = MessageBuilder.withPayload(message).setHeader(RocketMQHeaders.KEYS, cdrMessageDTO.getMainCdr().getCallId()).build();
                rocketMQTemplate.syncSend("cloudcc_outsidecdr_quality_topic" + StrUtil.COLON + qualityCompany, msg);
            }
            ArrayList<CdrDatapushPushEntity> rees = new ArrayList<>();
            for (CallCenterSubCdr callCenterSubCdr : subCdrs) {
                CdrDatapushPushEntity cdrDatapushPushEntity = accessRemote.getCdrDatapushPushEntity(cdrMessageDTO.getMainCdr(), cdrMessageDTO.getCdrChanneldata(), callCenterSubCdr, LOG_TAG);
                rees.add(cdrDatapushPushEntity);
            }
            log.info(LOG_TAG + "消息：{}，开始推送给第三方", message);
            accessRemote.handleCDR(LOG_TAG, companyCode, month, rees, dictItem);
            log.info(LOG_TAG + "消息：{}，消费成功", message);
        } catch (Exception e) {
            log.error(LOG_TAG + "消息：{}，处理异常，存入日志文件", message, e);
            CDR_RESOLVE_EXCEPTION_LOG.info("消息：{}，异常信息：", message, e);
        }
    }
}
