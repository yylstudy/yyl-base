package com.cqt.hmbc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.cqt.cloud.api.basesetting.BaseSettingFeignClient;
import com.cqt.common.constants.HmbcConstants;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.MessageTypeEnum;
import com.cqt.hmbc.config.RabbitMqConfig;
import com.cqt.hmbc.handler.MqProducer;
import com.cqt.hmbc.retry.RetryPushDTO;
import com.cqt.hmbc.service.CorpPushService;
import com.cqt.hmbc.service.PrivateDialTestPushDiscardRecordService;
import com.cqt.hmbc.util.RestTemplateRequest;
import com.cqt.model.common.MessageDTO;
import com.cqt.model.hmbc.entity.PrivateDialTestPushDiscardRecord;
import com.cqt.model.hmbc.properties.HmbcProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CorpPushServiceImpl
 *
 * @author Xienx
 * @date 2023年02月10日 14:57
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorpPushServiceImpl implements CorpPushService, HmbcConstants, PrivateCacheConstant {

    private final MqProducer mqProducer;
    private final RestTemplate restTemplate;
    private final RestTemplate lbRestTemplate;
    private final HmbcProperties hmbcProperties;
    private final BaseSettingFeignClient baseSettingAPI;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final PrivateDialTestPushDiscardRecordService discardRecordService;


    @Override
    public void pushBatch(List<RetryPushDTO> retryPushDTOList) {
        retryPushDTOList.forEach(this::pushWithRetry);
    }

    @Override
    public void pushWithRetry(RetryPushDTO retryPushDTO) {
        RestTemplate reqTemplate = restTemplate;
        if (hmbcProperties.getPushInfo().getFeignUrl().contains(retryPushDTO.getUrl())) {
            // 这里进行推送
            reqTemplate = lbRestTemplate;
        }
        pushWithRetry(reqTemplate, retryPushDTO);
    }


    private void pushWithRetry(RestTemplate restTemplate, RetryPushDTO retryPushDTO) {
        try {
            log.info("{}, 当前重试次数: {}", retryPushDTO.getBizId(), retryPushDTO.getRetryCount());
            RestTemplateRequest.of(restTemplate)
                    .post(retryPushDTO.getUrl())
                    .body(retryPushDTO.getBody())
                    .timeout(hmbcProperties.getPushInfo().getInterval())
                    .execute();
        } catch (Exception e) {
            log.error("{}, 出现异常: ", retryPushDTO.getBizId(), e);
            retryPushDTO.setFailCause("请求企业推送URL异常: " + e.getMessage());
            retryHandle(retryPushDTO);
        }
    }

    @Override
    public void retryHandle(RetryPushDTO retryPushDTO) {
        int retryCount = retryPushDTO.getRetryCount();
        int maxAttempts = hmbcProperties.getPushInfo().getMaxAttempts();
        if (canRetry(retryCount, maxAttempts)) {
            log.info("{}, 当前重试次数: {}, 未达到最大重试次数, 发送到延时队列", retryPushDTO.getBizId(), retryCount);
            mqProducer.sendDelayMsg(retryPushDTO, hmbcProperties.getPushInfo().getInterval(),
                    RabbitMqConfig.HMBC_RESULT_PUSH_DELAY_EXCHANGE, RabbitMqConfig.PUSH_ROUTING_KEY);
            return;
        }
        discardHandle(retryPushDTO);
    }

    @Override
    public void discardHandle(RetryPushDTO retryPushDTO) {
        log.warn("{}, 已达到最大重试次数: {}, 丢弃", retryPushDTO.getBizId(), retryPushDTO.getRetryCount());
        PrivateDialTestPushDiscardRecord discardRecord = new PrivateDialTestPushDiscardRecord();
        discardRecord.setVccId(retryPushDTO.getVccId())
                .setNumber(retryPushDTO.getNumber())
                .setType(retryPushDTO.getJobType())
                .setPushUrl(retryPushDTO.getUrl())
                .setPushJson(retryPushDTO.getBody())
                .setReason(retryPushDTO.getFailCause());
        discardRecordService.save(discardRecord);
        // 推送失败则进行钉钉告警
        ddingAlert(retryPushDTO.getJobType(), Collections.singletonMap(retryPushDTO.getNumber(), retryPushDTO.getFailCause()),
                retryPushDTO.getVccId(), retryPushDTO.getVccName(), retryPushDTO.getUrl());
    }


    /**
     * 推送企业失败的拨测结果进行钉钉告警
     *
     * @param errorInfos 推送失败的拨测结果
     * @param vccId      企业vccId
     * @param vccName    企业名称
     * @param pushUrl    企业推送URL
     */
    private void ddingAlert(int jobType, Map<String, String> errorInfos, String vccId, String vccName, String pushUrl) {
        String errorInfoStr = StrUtil.join(",", errorInfos);
        String title = "【定时隐私号拨测结果推送失败告警】";
        if (DialTestType.LOCATION_UPDATE_DIAL_TEST.equals(jobType)) {
            title = "【定时位置更新结果推送失败告警】";
        }
        String alertMsg = StrUtil.format("所属企业: {}({})\r\n推送地址: {}\r\n" +
                "总计推送失败: {}\r\n, 详情如下: {}", vccId, vccName, pushUrl, errorInfos.size(), errorInfoStr);

        ddingAlert(title, alertMsg);
    }

    /**
     * 钉钉告警
     *
     * @param title   告警标题
     * @param content 告警内容
     */
    private void ddingAlert(String title, String content) {
        String alertMsg = StrUtil.format("{} \r\n告警时间: {}\r\n当前设备: {}\r\n {}",
                title, DateUtil.now(), nacosDiscoveryProperties.getIp(), content);

        // 这里构造请求参数
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setType(MessageTypeEnum.dingding.name());
        messageDTO.setContent(alertMsg);
        messageDTO.setOperateType(title);
        baseSettingAPI.sendMessage(messageDTO);
    }
}
