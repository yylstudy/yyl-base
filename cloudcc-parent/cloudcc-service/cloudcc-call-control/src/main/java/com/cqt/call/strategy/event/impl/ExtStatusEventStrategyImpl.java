package com.cqt.call.strategy.event.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.EventEnum;
import com.cqt.base.enums.ext.ExtRegEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.converter.ModelConverter;
import com.cqt.call.event.store.ExtStatusActualStoreEvent;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.EventStrategy;
import com.cqt.model.cdr.entity.ExtStatusLog;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.dto.ExtStatusTransferDTO;
import com.cqt.model.freeswitch.dto.event.ExtensionStatusEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:48
 * 分机状态事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtStatusEventStrategyImpl implements EventStrategy {

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final RedissonClient redissonClient;

    // @Async(ThreadPoolConfig.BASE_POOL_NAME)
    @Override
    public void deal(String message) throws Exception {
        try {
            ExtensionStatusEventDTO statusEventDTO = objectMapper.readValue(message, ExtensionStatusEventDTO.class);
            ExtensionStatusEventDTO.EventData eventData = statusEventDTO.getData();
            String status = eventData.getStatus();
            // 分机状态事件消息幂等
            String id = statusEventDTO.getUuid() + statusEventDTO.getTimestamp();
            String idempotentLockKey = CacheUtil.getMessageIdempotentLockKey(id, status);
            Duration idempotent = cloudCallCenterProperties.getBase().getMessageIdempotent();
            boolean absent = redissonClient.getBucket(idempotentLockKey).setIfAbsent(1, idempotent);
            if (!absent) {
                log.warn("[分机状态事件] 消息重复已处理过, uuid: {}, status: {}", idempotentLockKey, status);
                return;
            }

            if (Objects.isNull(statusEventDTO.getTimestamp())) {
                statusEventDTO.setTimestamp(System.currentTimeMillis());
            }
            String extId = eventData.getExtId();
            log.info("[分机状态事件-{}] 消息: {}", status, message);
            String companyCode = statusEventDTO.getCompanyCode();
            if (StrUtil.isEmpty(companyCode)) {
                log.info("[分机状态事件] 分机: {}, 企业为空未分配!", extId);
                return;
            }
            // TODO 先调用底层接口查询下是否在通话中
            ExtStatusTransferDTO transferDTO = new ExtStatusTransferDTO();
            transferDTO.setExtIp(eventData.getRegAddr());
            transferDTO.setRegAddr(eventData.getRegAddr());
            transferDTO.setUuid(statusEventDTO.getUuid());
            transferDTO.setExtId(extId);
            transferDTO.setCompanyCode(companyCode);
            transferDTO.setTransferAction(ExtStatusTransferActionEnum.REGISTER.name());

            // 先查询分机的原有状态 redis
            ExtStatusDTO extStatusDTO = dataQueryService.getActualExtStatus(companyCode, extId);
            // UNREG:注销, REG:注册
            if (ExtRegEnum.REG.name().equals(status)) {
                // 上一次的目标状态
                String lastTargetTargetStatus = Optional.ofNullable(extStatusDTO)
                        .map(ExtStatusDTO::getTargetStatus)
                        .orElse(ExtStatusEnum.OFFLINE.name());
                // 只有原来是离线状态才更新为在线， 其他通话状态不处理
                if (ExtStatusEnum.OFFLINE.name().equals(lastTargetTargetStatus)) {
                    transferDTO.setSourceStatus(lastTargetTargetStatus);
                    // 上一次的进入目标状态的时间戳
                    Long lastTargetTimestamp = Optional.ofNullable(extStatusDTO)
                            .map(ExtStatusDTO::getTargetTimestamp)
                            .orElse(0L);
                    transferDTO.setSourceTimestamp(lastTargetTimestamp);
                    transferDTO.setTargetStatus(ExtStatusEnum.ONLINE.name());
                    transferDTO.setTargetTimestamp(statusEventDTO.getTimestamp());
                    transferDTO.setAgentId(dataQueryService.getAgentIdRelateExtId(companyCode, extId));
                    update(transferDTO);
                }
                return;
            }
            if (ExtRegEnum.UNREG.name().equals(status)) {
                // 上一次的目标状态
                String lastTargetTargetStatus = ExtStatusEnum.ONLINE.name();
                // 上一次的进入目标状态的时间戳
                Long lastTargetTimestamp = 0L;
                if (Optional.ofNullable(extStatusDTO).isPresent()) {
                    lastTargetTargetStatus = extStatusDTO.getTargetStatus();
                    lastTargetTimestamp = extStatusDTO.getTargetTimestamp();
                    transferDTO.setOs(extStatusDTO.getOs());
                }
                transferDTO.setSourceStatus(lastTargetTargetStatus);
                transferDTO.setSourceTimestamp(lastTargetTimestamp);
                transferDTO.setTargetStatus(ExtStatusEnum.OFFLINE.name());
                transferDTO.setTargetTimestamp(statusEventDTO.getTimestamp());
                transferDTO.setAgentId(dataQueryService.getAgentIdRelateExtId(companyCode, extId));
                update(transferDTO);
                // 分机离线, 是否需要把关联坐席签出??? 通知前端SDK
                ClientCallbackVO callbackVO = ClientCallbackVO.build(transferDTO);
                dataStoreService.notifyClient(callbackVO);
            }
        } catch (Exception e) {
            log.error("[分机状态事件] 处理异常: ", e);
        }
    }

    private void update(ExtStatusTransferDTO transferDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("[分机状态事件-{}] 状态变迁日志: {}", transferDTO.getTransferAction(), objectMapper.writeValueAsString(transferDTO));
        }
        // mysql 状态迁移日志, redis 分机状态 extStatus:{companyCode}:{extId}
        ExtStatusLog extStatusLog = ModelConverter.INSTANCE.extStatusTransfer2ExtStatusLog(transferDTO);
        applicationContext.publishEvent(new ExtStatusActualStoreEvent(this, extStatusLog));
    }

    @Override
    public EventEnum getEventType() {
        return EventEnum.ext_status;
    }
}
