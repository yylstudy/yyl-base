package com.cqt.broadnet.web.x.service.release;

import cn.hutool.core.util.StrUtil;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.broadnet.web.x.service.retry.StatusBillPushRetryImpl;
import com.cqt.common.util.AuthUtil;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date:  2023-04-12 11:08
 * 通话状态推送客户接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusBillStorePushService extends AbstractStorePushService {

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final ObjectMapper objectMapper;

    private final StatusBillPushRetryImpl statusBillPushRetryImpl;

    public void pushStatus(PrivateStatusInfo privateStatusInfo) throws JsonProcessingException {
        if (StrUtil.isEmpty(privateStatusInfo.getBindId())) {
            Optional<BindInfoApiVO> bindInfoVoOptional = privateCorpBusinessInfoService.getBindInfoVO(privateStatusInfo.getRecordId());
            privateStatusInfo.setBindId(bindInfoVoOptional.map(BindInfoApiVO::getBindId).orElse(""));
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(privateStatusInfo.getTelX());
        log.info("pushStatus vccId: {}, telX: {}", businessInfoDTO.getVccId(), privateStatusInfo.getTelX());
        // 设置ts和sign
        setSign(privateStatusInfo, businessInfoDTO);
        pushToCustomer(privateStatusInfo.getRecordId(),
                privateStatusInfo,
                businessInfoDTO.getVccId(),
                businessInfoDTO.getStatusPushFlag(),
                businessInfoDTO.getStatusPushUrl(),
                businessInfoDTO.getPushRetryNum(),
                Duration.ofMinutes(businessInfoDTO.getPushRetryMin()));

    }

    public void setSign(PrivateStatusInfo privateStatusInfo, PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO) {
        privateStatusInfo.setTs(System.currentTimeMillis());
        TreeMap<String, Object> treeMap = objectMapper.convertValue(privateStatusInfo, new TypeReference<TreeMap<String, Object>>() {
        });
        String sign = AuthUtil.createSign(treeMap, privateCorpBusinessInfoDTO.getVccId(), privateCorpBusinessInfoDTO.getSecretKey());
        privateStatusInfo.setSign(sign);
    }

    @Override
    public void pushDataToMq(PushRetryDataDTO pushRetryDataDTO) {
        statusBillPushRetryImpl.pushDataToMq(pushRetryDataDTO);
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
        return "通话状态";
    }
}
