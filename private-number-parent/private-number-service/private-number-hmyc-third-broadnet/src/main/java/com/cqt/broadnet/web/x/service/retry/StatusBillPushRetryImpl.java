package com.cqt.broadnet.web.x.service.retry;

import cn.hutool.core.convert.Convert;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.x.service.PrivateCdrRePushService;
import com.cqt.common.enums.BillPushResultEnum;
import com.cqt.model.common.Result;
import com.cqt.rabbitmq.retry.AbstractPushFailRetry;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author linshiqiang
 * date: 2023-04-12 11:08
 * 状态话单推送接口失败重试模板方法实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusBillPushRetryImpl extends AbstractPushFailRetry {

    private static final String SUFFIX_NAME = "private-number-hmyc-third-broadnet-status-bill-retry-push-";

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final PrivateCdrRePushService privateCdrRePushService;

    @Override
    protected void doErrorCodeResult(PushRetryDataDTO pushRetryDataDTO) {
        log.info("接口返回状态码不正确");
        // 入库
        pushRetryDataDTO.setErrorMessage(BillPushResultEnum.ERROR_RESULT_CODE.getMessage() + ": " + pushRetryDataDTO.getErrorMessage());
        privateCdrRePushService.savePrivateCdrRePush(pushRetryDataDTO);
    }

    @Override
    protected void doAfterReachMaxRetry(PushRetryDataDTO pushRetryDataDTO) {
        log.info("重试次数达到上限, 不再推送到mq延时, 后续操作");
        // 入库
        pushRetryDataDTO.setErrorMessage(BillPushResultEnum.REACH_MAX_RETRY.getMessage() + ": " + pushRetryDataDTO.getErrorMessage());
        privateCdrRePushService.savePrivateCdrRePush(pushRetryDataDTO);
    }

    @Override
    public Long increaseRetryCount(String uniqueId) {
        return redissonUtil.increment(uniqueId, Duration.ofHours(1));
    }

    @Override
    public Long getCurrentRetryCount(String uniqueId) {
        return Convert.toLong(redissonUtil.getString(uniqueId), 1L);
    }

    @Override
    public void deleteUniqueIdKey(String uniqueId) {
        redissonUtil.delKey(uniqueId);
    }

    @Override
    public Integer maxRetry() {
        return privateNumberBindProperties.getMaxRetry();
    }

    @Override
    public Integer getTimeout() {
        return privateNumberBindProperties.getPushTimeout();
    }

    @Override
    public Duration getInterval() {
        return privateNumberBindProperties.getInterval();
    }

    @Override
    public Boolean checkPushResult(String body) {
        // 检查接口返回结果 状态码code是否合法
        try {
            Result result = objectMapper.readValue(body, Result.class);
            return result.getCode() == 0;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    @Override
    public Boolean dlx() {
        return privateNumberBindProperties.getDlx();
    }

    @Override
    public String getDelayExchangeName() {
        return SUFFIX_NAME + "DelayExchange";
    }

    @Override
    public String getDelayQueueName() {
        return SUFFIX_NAME + "DelayQueue";
    }

    @Override
    public String getDeadExchangeName() {
        return SUFFIX_NAME + "DeadExchange";
    }

    @Override
    public String getDeadQueueName() {
        return SUFFIX_NAME + "DeadQueue";
    }
}
