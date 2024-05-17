package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CacheConstant;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.exception.BizException;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.CallContextService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.freeswitch.dto.api.GetSessionVarDTO;
import com.cqt.model.freeswitch.vo.GetSessionVarVO;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-08-22 9:41
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallContextServiceImpl implements CallContextService {

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final Snowflake snowflake;

    @Override
    public CallUuidContext getCallUuidContext(String companyCode, String uuid) {
        String lockKey = CacheUtil.getUuidContextUpdateLockKey(uuid);
        RLock fairLock = redissonUtil.getFairLock(lockKey);
        fairLock.lock(cloudCallCenterProperties.getLockTIme().toMillis(), TimeUnit.MILLISECONDS);
        try {
            String callUuidKey = CacheUtil.getCallUuidKey(companyCode, uuid);
            return redissonUtil.get(callUuidKey, CallUuidContext.class);
        } catch (Exception e) {
            log.error("[查询uuid上下文] 企业: {}, uuid: {}, 异常: ", companyCode, uuid, e);
        } finally {
            try {
                fairLock.unlock();
            } catch (Exception e) {
                log.error("[getCallUuidContext] fairLock unlock error: ", e);
            }
        }
        return null;
    }

    @Override
    public void saveCallUuidContext(CallUuidContext callUuidContext) {
        String lockKey = CacheUtil.getUuidContextUpdateLockKey(callUuidContext.getUUID());
        RLock fairLock = redissonUtil.getFairLock(lockKey);
        fairLock.lock(cloudCallCenterProperties.getLockTIme().toMillis(), TimeUnit.MILLISECONDS);
        try {
            String data = objectMapper.writeValueAsString(callUuidContext);
            CallUuidRelationDTO current = callUuidContext.getCurrent();
            String callUuidKey = CacheUtil.getCallUuidKey(current.getCompanyCode(), current.getUuid());
            if (log.isDebugEnabled()) {
                log.debug("[通话uuid上下文] json数据: {}", data);
            }
            // ttl 12h
            boolean set = redissonUtil.setString(callUuidKey, data, CacheConstant.TTL, TimeUnit.HOURS);
            log.info("[通话uuid上下文] key: {}, 保存redis结果: {}", callUuidKey, set);
        } catch (Exception e) {
            log.error("[saveCallUuidContext] error: ", e);
        } finally {
            try {
                fairLock.unlock();
            } catch (Exception e) {
                log.error("[saveCallUuidContext] fairLock unlock error: ", e);
            }
        }
    }

    @Override
    public void addInCallNumbers(String companyCode, String mainCallId, String number, String uuid) {
        try {
            String inCallNumbersKey = CacheUtil.getInCallNumbersKey(companyCode, mainCallId);
            redissonUtil.setHash(inCallNumbersKey, number, uuid, Duration.ofHours(CacheConstant.TTL));
        } catch (Exception e) {
            log.error("[addInCallNumbers] 企业: {}, 通话id: {}, numbers: {}, error: ",
                    companyCode, mainCallId, number, e);
        }
    }

    @Override
    public void deleteInCallNumbers(String companyCode, String mainCallId, String number, String uuid) {
        try {
            String inCallNumbersKey = CacheUtil.getInCallNumbersKey(companyCode, mainCallId);
            redissonUtil.removeHashItem(inCallNumbersKey, number);
        } catch (Exception e) {
            log.error("[deleteInCallNumbers] 企业: {}, 通话id: {}, number: {}, error: ",
                    companyCode, mainCallId, number, e);
        }
    }

    @Override
    public Set<String> getInCallNumbers(String companyCode, String mainCallId) {
        try {
            String inCallNumbersKey = CacheUtil.getInCallNumbersKey(companyCode, mainCallId);
            Map<String, String> map = redissonUtil.readAllHash(inCallNumbersKey);
            if (CollUtil.isEmpty(map)) {
                return new HashSet<>();
            }
            return map.keySet();
        } catch (Exception e) {
            log.error("[getInCallNumbers] 企业: {}, 通话id: {}, error: ", companyCode, mainCallId, e);
        }
        return null;
    }

    @Override
    public Set<String> getInCallNumbers(AgentStatusDTO agentStatusDTO) {
        if (AgentStatusEnum.CALLING.name().equals(agentStatusDTO.getTargetStatus())) {
            String uuid = agentStatusDTO.getUuid();
            String companyCode = agentStatusDTO.getCompanyCode();
            CallUuidContext callUuidContext = getCallUuidContext(companyCode, uuid);
            if (Objects.nonNull(callUuidContext)) {
                Boolean hangupFlag = callUuidContext.getCallCdrDTO().getHangupFlag();
                if (!Boolean.TRUE.equals(hangupFlag)) {
                    String mainCallId = callUuidContext.getMainCallId();
                    Set<String> inCallNumbers = getInCallNumbers(companyCode, mainCallId);
                    if (CollUtil.isNotEmpty(inCallNumbers)) {
                        inCallNumbers.remove(agentStatusDTO.getExtId());
                        return inCallNumbers;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Boolean isHangup(String companyCode, String uuid) {
        try {
            String uuidHangupFlagKey = CacheUtil.getUuidHangupFlagKey(companyCode, uuid);
            String data = redissonUtil.get(uuidHangupFlagKey);
            return StrUtil.isNotEmpty(data);
        } catch (Exception e) {
            log.error("[isHangup] 企业: {}, 通话id: {}, error: ", companyCode, uuid, e);
        }
        return null;
    }

    @Override
    public void saveCdrLink(String companyCode, String mainCallId, String sourceUUID, String destUUID) {
        String cdrLinkKey = CacheUtil.getCdrLinkKey(companyCode, mainCallId);
        String value = sourceUUID + StrUtil.COLON + destUUID;
        boolean set = redissonUtil.addSet(cdrLinkKey, value, Duration.ofHours(CacheConstant.TTL));
        log.info("[话单] 企业: {}, 主通话id: {}, 主叫uuid: {}, 被叫uuid: {}, set: {}",
                companyCode, mainCallId, sourceUUID, destUUID, set);
        // 是否持久化
    }

    @Override
    public Boolean isCdrGenerated(String companyCode, String mainCallId) {
        String cdrGenerateFlagKey = CacheUtil.getCdrGenerateFlagKey(companyCode, mainCallId);
        String data = redissonUtil.get(cdrGenerateFlagKey);
        return StrUtil.isNotEmpty(data);
    }

    @Override
    public <T> T getChannelVariable(GetSessionVarDTO getSessionVarDTO, Class<T> clazz) throws Exception {
        GetSessionVarVO getSessionVarVO = freeswitchRequestService.getSessionVar(getSessionVarDTO);
        if (getSessionVarVO.getResult()) {
            String value = getSessionVarVO.getValue();
            if (StrUtil.isEmpty(value)) {
                return null;
            }
            return objectMapper.readValue(value, clazz);
        }
        return null;
    }

    @Override
    public void saveCdrPlatformNumber(String companyCode, String mainCallId, String platformNumber) {
        try {
            String cdrChargeKey = CacheUtil.getCdrChargeKey(companyCode, mainCallId);
            redissonUtil.set(cdrChargeKey, platformNumber, CacheConstant.TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("[saveCdrPlatformNumber] 企业: {}, 通话id: {}, error: ", companyCode, mainCallId, e);
        }
    }

    @Override
    public String getPlatformNumber(String companyCode, String mainCallId) {
        try {
            String cdrChargeKey = CacheUtil.getCdrChargeKey(companyCode, mainCallId);
            return redissonUtil.get(cdrChargeKey);
        } catch (Exception e) {
            log.error("[getPlatformNumber] 企业: {}, 通话id: {}, error: ", companyCode, mainCallId, e);
        }
        return null;
    }

    @Override
    public String createMainCallId() {
        return snowflake.nextIdStr();
    }

    @Override
    public Boolean saveConsultFlag(String uuid) {
        String consultLockKey = CacheUtil.getConsultLockKey(uuid);
        return redissonUtil.setNx(consultLockKey, System.currentTimeMillis(), Duration.ofSeconds(60));
    }

    @Override
    public void saveRecordFile(String companyCode, String uuid, String filePath) {
        try {
            if (StrUtil.isNotEmpty(filePath)) {
                String recordFileKey = CacheUtil.getUuidRecordFileKey(companyCode, uuid);
                redissonUtil.set(recordFileKey, filePath, Duration.ofHours(CacheConstant.TTL));
            }
        } catch (Exception e) {
            log.error("[saveRecordFile] 企业: {}, uuid: {}, filePath: {}, error: ", companyCode, uuid, filePath, e);
        }
    }

    @Override
    public String getRecordFile(String companyCode, String uuid) {
        return redissonUtil.get(CacheUtil.getUuidRecordFileKey(companyCode, uuid));
    }

    @Override
    public void saveToken(String companyCode, String agentId, String os, String token) {
        try {
            String tokenKey = CacheUtil.getTokenKey(companyCode, os, agentId);
            redissonUtil.set(tokenKey, token, Duration.ofDays(CacheConstant.TTL_2D));
            // set token
            String tokenInfoKey = CacheUtil.getTokenInfoKey(token);
            String value = companyCode + StrUtil.COLON + os + StrUtil.COLON + agentId;
            redissonUtil.set(tokenInfoKey, value, Duration.ofDays(CacheConstant.TTL_2D));
            log.info("[saveToken] 企业: {}, 坐席: {}, token: {}", companyCode, agentId, token);
        } catch (Exception e) {
            throw new BizException(SdkErrCode.GET_TOKEN_FAIL, e);
        }
    }

    @Override
    public boolean checkToken(String sdkToken) {
        try {
            String tokenInfoKey = CacheUtil.getTokenInfoKey(sdkToken);
            String tokenInfo = redissonUtil.get(tokenInfoKey);
            if (StrUtil.isEmpty(tokenInfo)) {
                return false;
            }
            String[] info = StrUtil.splitToArray(tokenInfo, StrUtil.COLON);
            String companyCode = info[0];
            String os = info[1];
            String agentId = info[2];
            String tokenKey = CacheUtil.getTokenKey(companyCode, os, agentId);
            String token = redissonUtil.get(tokenKey);
            log.info("[checkToken] 企业: {}, os: {}, 坐席: {}, token: {}", companyCode, os, agentId, token);
            return StrUtil.isNotEmpty(token) && token.equals(sdkToken);
        } catch (Exception e) {
            log.error("[checkToken] token: {}, error: ", sdkToken, e);
            return true;
        }
    }
}
