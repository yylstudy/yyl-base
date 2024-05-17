package com.cqt.cdr.cloudccsfaftersales.listener;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.cqt.base.contants.CommonConstant;
import com.cqt.cdr.cloudccsfaftersales.conf.DynamicConfig;
import com.cqt.cdr.cloudccsfaftersales.entity.DictItem;
import com.cqt.cdr.cloudccsfaftersales.mapper.DictMapper;
import com.cqt.cdr.cloudccsfaftersales.service.DictItemService;
import com.cqt.cdr.cloudccsfaftersales.service.PushErrService;
import com.cqt.cdr.cloudccsfaftersales.util.AccessRemote;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.dto.RemoteQualityCdrDTO;
import com.cqt.model.cdr.vo.RemoteCdrVO;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * @author xinson
 * date:  2023-08-09 19:14
 * 内部话单生成
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "cloudcc_outsidecdr_quality_topic", consumerGroup = "cloudcc_outsidecdr_quality-group")
public class CdrQualityListener implements RocketMQListener<String> {private static final Logger CDR_RESOLVE_EXCEPTION_LOG = LoggerFactory.getLogger("cdrResolveExceptionLogger");

    @Resource
    private DynamicConfig dynamicConfig;

    @Resource
    private DictItemService dictItemService;

    @Resource
    private AccessRemote accessRemote;

    @Resource
    private PushErrService pushErrService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(String message) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 质检话单消费 | ";
        try {
            log.info(LOG_TAG + "接收到MQ消息: " + message);
            CdrMessageDTO cdrMessageDTO = CdrMessageDTO.getCdrMessageDTO(message, objectMapper, LOG_TAG);
            if (cdrMessageDTO == null) {
                return;
            }
            String companyCode = cdrMessageDTO.getMainCdr().getCompanyCode();
            String month = DateUtil.format(cdrMessageDTO.getMainCdr().getHangupTime(), CommonConstant.MONTH_FORMAT);
            RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);

            List<DictItem> dictItems = dictItemService.getPushInformation(LOG_TAG, companyCode);
            if (dictItems.isEmpty()) {
                log.warn(LOG_TAG + "企业{}，配置路径不存在写入数据库: " + message, companyCode);
                pushErrService.errIntoErrTable(message, null, "2", month, companyCode, LOG_TAG, null);
                return;
            }
            Map<String, Object> remoteCdrDTOs = accessRemote.getRemoteCdrDTO(cdrMessageDTO, companyCode);
            List<String> qualityCompanys = dynamicConfig.getQualityAllPush();
            for (String qualityCompany : qualityCompanys) {
                if (companyCode.equals(qualityCompany)) {
                    log.info(LOG_TAG + "企业{}，进行全量推送 ", qualityCompany);
                    for (DictItem dictItem : dictItems) {
                        switch (dictItem.getItemText()) {
                            case "quality":
                                accessThirdParties(LOG_TAG, companyCode, month, remoteCdrDTOs, dictItem, "quality");
                                break;
                            case "aftersale":
                                accessThirdParties(LOG_TAG, companyCode, month, remoteCdrDTOs, dictItem, "aftersale");
                                break;
                            case "busy":
                                accessThirdParties(LOG_TAG, companyCode, month, remoteCdrDTOs, dictItem, "busy");
                                break;
                            default:
                                log.warn(LOG_TAG + "企业{}，进行全量推送，配置路径不存在写入数据库: " + message, companyCode);
                                pushErrService.errIntoErrTable(message, null, "2", month, companyCode, LOG_TAG, null);
                        }
                    }
                    return;
                }
            }
            DictItem dictItem = dictItemService.getQualityUrl(dictItems);
            if (dictItem == null || dictItem.getItemValue() == null) {
                log.warn(LOG_TAG + "配置路径不存在写入数据库: " + message);
                pushErrService.errIntoErrTable(message, null, "2", month, companyCode, LOG_TAG, "quality");
                return;
            }
            accessThirdParties(LOG_TAG, companyCode, month, remoteCdrDTOs, dictItem, "quality");
            log.info(LOG_TAG + "消息：{}，消费成功", message);
        } catch (Exception e) {
            log.error(LOG_TAG + "消息：{}，处理异常，存入日志文件", message,e);
            CDR_RESOLVE_EXCEPTION_LOG.info("消息：{}，异常信息：", message, e);
        }
    }

    private void accessThirdParties(String LOG_TAG, String companyCode, String month, Map<String, Object> remoteCdrDTOs, DictItem dictItem, String type) {
        log.info(LOG_TAG + "访问第三方接口，推送话单类型：{}", type);
        RemoteCdrVO remoteCdrVO;
        if (StringUtils.isNotEmpty(type) && (type.equals("busy") || type.equals("aftersale"))) {
            List list = (List) remoteCdrDTOs.get(type);
            for (Object o : list) {
                remoteCdrVO = accessRemote.resendQualityCdr(dictItem, o, LOG_TAG);
                if (remoteCdrVO == null || !"200".equals(remoteCdrVO.getCode())) {
                    Object remoteQualityCdrDTOPushErrorJson = remoteCdrDTOs.get(type);
                    String json = JSONObject.toJSONString(remoteQualityCdrDTOPushErrorJson);
                    log.warn(LOG_TAG + "重推失败，写入数据库：{}", json);
                    pushErrService.errIntoErrTable(json, dictItem.getItemValue(), "1", month, companyCode, LOG_TAG, type);
                }
            }
        } else {
            remoteCdrVO = accessRemote.resendQualityCdr(dictItem, remoteCdrDTOs.get(type), LOG_TAG);
            if (remoteCdrVO == null || !"200".equals(remoteCdrVO.getCode())) {
                Object remoteQualityCdrDTOPushErrorJson = remoteCdrDTOs.get(type);
                String json = JSONObject.toJSONString(remoteQualityCdrDTOPushErrorJson);
                log.warn(LOG_TAG + "重推失败，写入数据库：{}", json);
                pushErrService.errIntoErrTable(json, dictItem.getItemValue(), "1", month, companyCode, LOG_TAG, type);
            }
        }
    }
}
