package com.cqt.sdk.client.strategy.client.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.cache.AgentCheckinCache;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientCheckoutDTO;
import com.cqt.model.client.validategroup.AgentIdGroup;
import com.cqt.model.client.validategroup.ExtIdGroup;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.vo.GetExtensionRegVO;
import com.cqt.sdk.client.converter.ModelConverter;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.agentstatus.OfflineAgentQueueEvent;
import com.cqt.sdk.client.event.arrange.CallStopArrangeCancelEvent;
import com.cqt.sdk.client.event.mq.AgentStatusLogStoreEvent;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 签出操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final RedissonClient redissonClient;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.checkout;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientCheckoutDTO clientCheckoutDTO = convert(requestBody, ClientCheckoutDTO.class,
                AgentIdGroup.class, ExtIdGroup.class);
        // lock
        String checkoutLockKey = CacheUtil.getCheckoutLockKey(clientCheckoutDTO.getCompanyCode(), clientCheckoutDTO.getAgentId());
        RLock fairLock = redissonClient.getFairLock(checkoutLockKey);
        boolean tryLock = fairLock.tryLock(cloudCallCenterProperties.getLockTIme().toMillis(), TimeUnit.MILLISECONDS);
        if (!tryLock) {
            return ClientResponseBaseVO.response(clientCheckoutDTO, "1", "签出失败, 坐席已发起签出, 请稍后重试!");
        }
        try {
            return checkout(clientCheckoutDTO);
        } finally {
            fairLock.unlock();
        }
    }

    private ClientResponseBaseVO checkout(ClientCheckoutDTO clientCheckoutDTO) throws JsonProcessingException {
        String companyCode = clientCheckoutDTO.getCompanyCode();
        String agentId = clientCheckoutDTO.getAgentId();
        // 查询当前状态
        Optional<AgentStatusDTO> agentStatusOptional = dataQueryService.getAgentStatusDTO(companyCode, agentId);
        if (!agentStatusOptional.isPresent()) {
            return ClientResponseBaseVO.response(clientCheckoutDTO, "1", "签出失败!");
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        if (log.isInfoEnabled()) {
            log.info("[签出] 当前坐席状态: {}", objectMapper.writeValueAsString(agentStatusDTO));
        }

        // 删除签入记录
        dataStoreService.removeCheckInRecord(clientCheckoutDTO);

        // 挂断话务
        hangup(agentStatusDTO);

        long currentTimestamp = System.currentTimeMillis();
        // 坐席实时状态
        agentStatusDTO.setNull();
        agentStatusDTO.setSourceStatus(agentStatusDTO.getTargetStatus());
        agentStatusDTO.setSourceSubStatus(agentStatusDTO.getTargetSubStatus());
        agentStatusDTO.setSourceDuration(agentStatusDTO.getTargetDuration());
        agentStatusDTO.setSourceTimestamp(agentStatusDTO.getTargetTimestamp());
        agentStatusDTO.setTransferAction(AgentStatusTransferActionEnum.CHECKOUT.name());
        agentStatusDTO.setTargetStatus(AgentStatusEnum.OFFLINE.name());
        agentStatusDTO.setTargetTimestamp(currentTimestamp);
        agentStatusDTO.setCheckoutTime(currentTimestamp);
        dataStoreService.updateActualAgentStatus(agentStatusDTO);

        // 迁出操作完成, 发送迁出日志记录事件
        AgentStatusTransferDTO agentStatusTransferDTO = ModelConverter.INSTANCE.status2transfer(agentStatusDTO);
        applicationContext.publishEvent(new AgentStatusLogStoreEvent(this, agentStatusTransferDTO));

        Integer serviceMode = agentStatusDTO.getServiceMode();
        AgentServiceModeEnum serviceModeEnum = AgentServiceModeEnum.parse(serviceMode);
        // 企业坐席空闲队列-移除
        applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                companyCode, agentId, serviceModeEnum, currentTimestamp, OperateTypeEnum.DELETE));
        log.info("[签出] 企业坐席空闲队列-删除, 企业: {}, 坐席: {}", companyCode, agentId);

        // 企业坐席离线队列-新增
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, serviceModeEnum, currentTimestamp, OperateTypeEnum.INSERT));
        log.info("[签出] 企业坐席离线队列-新增, 企业: {}, 坐席: {}", companyCode, agentId);

        // 外呼型 移除
        applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                companyCode, agentId, AgentServiceModeEnum.OUTBOUND, currentTimestamp, OperateTypeEnum.DELETE));
        log.info("[签出] 外呼型-企业坐席空闲队列-删除, 企业: {}, 坐席: {}", companyCode, agentId);

        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, AgentServiceModeEnum.OUTBOUND, currentTimestamp, OperateTypeEnum.DELETE));
        log.info("[签出] 外呼型-企业坐席离线队列-删除, 企业: {}, 坐席: {}", companyCode, agentId);

        // 检测分机状态
        ExtStatusDTO extStatusDTO = dataQueryService.getActualExtStatus(companyCode, agentStatusDTO.getExtId());
        GetExtensionRegVO extRealRegStatus = dataQueryService.getExtRealRegStatus(companyCode, agentStatusDTO.getExtId());
        dataStoreService.compareExtStatus(extStatusDTO, extRealRegStatus);

        // 通知SDK状态变化
        dataStoreService.notifySdkAgentStatus(ClientAgentStatusChangeVO.build(agentStatusDTO));

        // 移除事后处理任务
        applicationContext.publishEvent(new CallStopArrangeCancelEvent(this, companyCode, agentId));

        AgentCheckinCache.remove(companyCode, agentId);
        return ClientResponseBaseVO.response(clientCheckoutDTO, "0", "签出成功", clientCheckoutDTO.getReply());
    }


    private void hangup(AgentStatusDTO agentStatusDTO) {
        String targetStatus = agentStatusDTO.getTargetStatus();
        if (AgentStatusEnum.RINGING.name().equals(targetStatus)
                || AgentStatusEnum.CALLING.name().equals(targetStatus)) {
            String companyCode = agentStatusDTO.getCompanyCode();
            String uuid = agentStatusDTO.getUuid();
            // 坐席离线
            CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
            callUuidContext.getCurrent().setCheckoutToOffline(true);
            commonDataOperateService.saveCallUuidContext(callUuidContext);
            log.info("[签出] 企业: {}, uuid: {}, 挂断话务", companyCode, uuid);

            if (StrUtil.isEmpty(uuid)) {
                return;
            }
            HangupDTO hangupDTO = HangupDTO.build(companyCode, true, uuid, HangupCauseEnum.CHECKOUT);
            freeswitchRequestService.hangup(hangupDTO);
        }
    }

}
