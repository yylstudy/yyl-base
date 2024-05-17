package com.cqt.call.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CacheConstant;
import com.cqt.base.enums.CallTypeEnum;
import com.cqt.base.enums.CallbackActionEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.service.DataStoreService;
import com.cqt.cloudcc.manager.config.CommonThreadPoolConfig;
import com.cqt.cloudcc.manager.service.CallContextService;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.rpc.client.ClientServerRemoteService;
import com.cqt.rpc.queue.CallTaskRemoteService;
import com.cqt.rpc.queue.QueueControlRemoteService;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-07-06 15:01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataStoreServiceImpl implements DataStoreService {

    private final RedissonUtil redissonUtil;

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    private final CallContextService callContextService;

    private final CommonDataOperateService commonDataOperateService;

    @DubboReference
    private SdkInterfaceRemoteService sdkInterfaceRemoteService;

    @DubboReference
    private QueueControlRemoteService queueControlRemoteService;

    @DubboReference
    private ClientServerRemoteService clientServerRemoteService;

    @DubboReference
    private CallTaskRemoteService callTaskRemoteService;

    @Override
    public void addOfflineAgentQueue(String companyCode, String agentId, Long timestamp, String phoneNumber) {
        try {
            sdkInterfaceRemoteService.addOfflineAgentQueue(companyCode, agentId, timestamp, phoneNumber);
        } catch (Exception e) {
            log.error("[addOfflineAgentQueue] companyCode: {}, agentId: {}, phoneNumber: {}, error: ",
                    companyCode, agentId, phoneNumber, e);
        }
    }

    @Override
    public Boolean addUserLevelQueue(UserQueueUpDTO userQueueUpDTO) {
        try {
            return queueControlRemoteService.addUserLevelQueue(userQueueUpDTO);
        } catch (Exception e) {
            log.error("[坐席未接] userQueueUpDTO: {}, 重新加入排队队列 异常:", userQueueUpDTO, e);
        }
        return false;
    }

    @Override
    public void updateActualExtStatus(ExtStatusDTO extStatusDTO) {
        commonDataOperateService.updateActualExtStatus(extStatusDTO);
    }

    @Override
    public Boolean agentStatusChangeTransfer(AgentStatusTransferBO agentStatusTransferBO) {
        try {
            return sdkInterfaceRemoteService.agentStatusChangeTransfer(agentStatusTransferBO);
        } catch (Exception e) {
            log.error("[agentStatusChangeTransfer] data: {}, rpc error: ", agentStatusTransferBO, e);
        }
        return false;
    }

    @Override
    public void saveCallUuidContext(CallUuidContext callUuidContext) {

        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }

    @Override
    public void delCallUuidContext(String companyCode, String uuid) {
        String callUuidKey = CacheUtil.getCallUuidKey(companyCode, uuid);
        redissonUtil.delKey(callUuidKey);
    }

    @Override
    public ResultVO<CallInIvrActionVO> distributeAgent(CallInIvrActionDTO callInIvrActionDTO) {
        try {
            return queueControlRemoteService.distributeAgent(callInIvrActionDTO);
        } catch (Exception e) {
            log.error("[notifyClient] data: {}, rpc error: ", callInIvrActionDTO, e);
            return ResultVO.fail(500, "rpc fail");
        }
    }

    @Override
    public void notifyClient(Object message) {
        try {
            if (message instanceof String) {
                clientServerRemoteService.request((String) message);
                return;
            }
            String data = objectMapper.writeValueAsString(message);
            log.info("[notifyClient] request data: {}", data);
            ClientResponseBaseVO clientResponseBaseVO = clientServerRemoteService.request(data);
            log.info("[notifyClient] response data: {}", clientResponseBaseVO);
        } catch (Exception e) {
            log.error("[notifyClient] message: {}, rpc error: ", message, e);
        }
    }

    @Override
    public void makeAgentBusy(String companyCode, String agentId) {
        try {
            sdkInterfaceRemoteService.makeAgentBusy(companyCode, agentId);
        } catch (Exception e) {
            log.error("[makeAgentBusy] 企业: {}, 坐席: {}, rpc error: ", companyCode, agentId, e);
        }
    }

    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @Override
    public void saveUuidHangupFlag(String companyCode, String uuid, Long timestamp) {
        String uuidHangupFlagKey = CacheUtil.getUuidHangupFlagKey(companyCode, uuid);
        if (Objects.isNull(timestamp)) {
            timestamp = System.currentTimeMillis();
        }
        try {
            redissonUtil.set(uuidHangupFlagKey, timestamp, CacheConstant.TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("[挂断事件] 企业: {}, uuid: {}, 保存uuid挂断标志异常: ", companyCode, uuid, e);
        }
    }

    @Override
    public void saveCdrLink(String companyCode, String mainCallId, String sourceUUID, String destUUID) {

        callContextService.saveCdrLink(companyCode, mainCallId, sourceUUID, destUUID);
    }

    @Override
    public void saveCdrChannelData(String companyCode, String uuid, String channelData) {
        String cdrChannelDataKey = CacheUtil.getCdrChannelDataKey(companyCode, uuid);
        redissonUtil.set(cdrChannelDataKey, channelData, CacheConstant.TTL, TimeUnit.HOURS);
    }

    @Override
    public boolean removeQueueUp(UserQueueUpDTO userQueueUpDTO) {
        try {
            return queueControlRemoteService.removeQueueUp(userQueueUpDTO);
        } catch (Exception e) {
            log.error("[移除排队-rpc] 企业: {}, 来电uuid: {}, 移除: ", userQueueUpDTO.getCompanyCode(), userQueueUpDTO.getUuid(), e);
        }
        return false;
    }

    @Override
    public void addInCallNumbers(String companyCode, String mainCallId, String number, String uuid) {
        callContextService.addInCallNumbers(companyCode, mainCallId, number, uuid);
    }

    @Override
    public void deleteInCallNumbers(String companyCode, String mainCallId, String number, String uuid) {
        callContextService.deleteInCallNumbers(companyCode, mainCallId, number, uuid);
    }

    @Override
    public void saveCdrGenerateFlag(String companyCode, String mainCallId) {
        try {
            String cdrGenerateFlagKey = CacheUtil.getCdrGenerateFlagKey(companyCode, mainCallId);
            redissonUtil.set(cdrGenerateFlagKey, System.currentTimeMillis(), CacheConstant.TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("[saveCdrGenerateFlag] 企业: {}, 通话id: {}, error: ", companyCode, mainCallId, e);
        }
    }

    @Override
    public Boolean cancelArrangeTask(String companyCode, String agentId) {
        try {
            return sdkInterfaceRemoteService.cancelArrangeTaskRpc(companyCode, agentId);
        } catch (Exception e) {
            log.error("[cancelArrangeTask] 企业: {}, 坐席: {}, rpc error: ", companyCode, agentId, e);
        }
        return false;
    }

    @Override
    public Boolean threeWayHangupAll(String companyCode, String mainCallId, String uuid) {
        // lock
        String threeWayRemainCallLockKey = CacheUtil.getThreeWayRemainCallLockKey(companyCode, mainCallId);
        RLock lock = redissonClient.getFairLock(threeWayRemainCallLockKey);
        lock.lock();
        try {
            String inCallNumbersKey = CacheUtil.getInCallNumbersKey(companyCode, mainCallId);
            Map<String, String> map = redissonUtil.readAllHash(inCallNumbersKey);
            if (CollUtil.isEmpty(map)) {
                return true;
            }
            Collection<String> values = map.values();
            log.info("[三方通话] 企业: {}, mainCallId: {}, uuid: {}, 剩余通话: {} ", companyCode, mainCallId, uuid, values);
            // values.size() == 2 剩余两人不再是三方通话
            resetThreeWay(companyCode, map);
            return values.size() <= 1;
        } catch (Exception e) {
            log.error("[三方通话] 企业: {}, mainCallId: {}, uuid: {}, 判断剩余通话人数异常: ", companyCode, mainCallId, uuid, e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    private void resetThreeWay(String companyCode, Map<String, String> map) {
        if (CollUtil.isEmpty(map)) {
            return;
        }
        if (map.size() == 2) {
            Collection<String> values = map.values();
            for (String uuid : values) {
                CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
                callUuidContext.getCurrent().setThreeWay(false);
                commonDataOperateService.saveCallUuidContext(callUuidContext);
                // 一方挂断, 通知另外两方坐席
                if (CallTypeEnum.AGENT.equals(callUuidContext.getCallTypeEnum())) {
                    String agentId = callUuidContext.getAgentId();
                    Set<String> set = new HashSet<>(map.keySet());
                    set.remove(agentId);
                    notifyClient(ClientCallbackVO.buildEnd(callUuidContext, CallbackActionEnum.THREE_WAY, set));
                    log.info("[挂断事件] 企业: {}, 三方通话剩两方: {}, 通知坐席SDK", companyCode, map);
                }
            }
        }
    }

    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @Override
    public void answerIvrNotice(String companyCode, String taskId, String value) {
        try {
            callTaskRemoteService.answerIvrNotice(companyCode, taskId, value);
        } catch (Exception e) {
            log.error("[ivr通知] 企业: {}, 任务id: {}, 值: {}, 通知ivr异常: ", companyCode, taskId, value, e);
        }
    }

    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @Override
    public void answerPredictNotice(String companyCode, String taskId, String member) {
        try {
            callTaskRemoteService.answerPredictNotice(companyCode, taskId, member);
        } catch (Exception e) {
            log.error("[predict通知] 企业: {}, 任务id: {}, 值: {}, 通知predict异常: ", companyCode, taskId, member, e);
        }
    }

    @Override
    public void saveAgentConsultFlag(String uuid) {
        String agentConsultKey = CacheUtil.getAgentConsultKey(uuid);
        redissonUtil.set(agentConsultKey, System.currentTimeMillis(), Duration.ofDays(CacheConstant.TTL));
    }

    @Override
    public void delAgentConsultFlag(String uuid) {
        String agentConsultKey = CacheUtil.getAgentConsultKey(uuid);
        redissonUtil.delKey(agentConsultKey);
    }

    @Override
    public boolean isAgentConsult(String bridgeUUID) {
        if (StrUtil.isEmpty(bridgeUUID)) {
            return false;
        }
        String agentConsultKey = CacheUtil.getAgentConsultKey(bridgeUUID);
        return StrUtil.isNotEmpty(redissonUtil.get(agentConsultKey));
    }

    @Override
    public boolean isOnlyOneCalling(String companyCode, String mainCallId) {
        String inCallNumbersKey = CacheUtil.getInCallNumbersKey(companyCode, mainCallId);
        Integer inCallCount = redissonUtil.getHashCount(inCallNumbersKey);
        return inCallCount == 1;
    }

    @Override
    public void unlockOriginate(String extId) {
        String originateLockKey = CacheUtil.getOriginateLockKey(extId);
        redissonUtil.delKey(originateLockKey);
    }

    @Override
    public Boolean unlockOriginate(String companyCode, String number) {
        String originateLockKey = CacheUtil.getOriginateLockKey(companyCode + StrUtil.COLON + number);
        return redissonUtil.delKey(originateLockKey);
    }

    @Override
    public Boolean lockOriginate(String companyCode, String number, String uuid) {
        String originateLockKey = CacheUtil.getOriginateLockKey(companyCode + StrUtil.COLON + number);
        return redissonUtil.setNx(originateLockKey, uuid, Duration.ofSeconds(10));
    }

    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    public void setCallTimestamp(String taskId, String item) {
        // TODO hash too big???
        String ivrTaskNumberCallStatusKey = CacheUtil.getIvrTaskNumberCallStatusKey(item);
        redissonUtil.setHash(ivrTaskNumberCallStatusKey, item, System.currentTimeMillis());
    }
}
