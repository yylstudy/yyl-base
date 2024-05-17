package com.cqt.rabbitmq.retry;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.rabbitmq.dynamic.DynamicContainerFactory;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.cqt.rabbitmq.util.RabbitmqUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-02-16 16:11
 * 基于rabbitmq死信交换机+redis实现推送接口重试
 */
@Slf4j
public abstract class AbstractPushFailRetry {

    public void addMessageListener(Boolean autoStartup) throws Exception {
        DynamicContainerFactory factory = SpringUtil.getBean(DynamicContainerFactory.class);
        factory.createListenerContainer(getDeadQueueName(), getDeadQueueName(), autoStartup);
        factory.start();
    }

    public void createDlx() {
        RabbitmqUtil rabbitmqUtil = SpringUtil.getBean(RabbitmqUtil.class);
        if (dlx()) {
            // 死信队列 + ttl
            rabbitmqUtil.createDlxQueueAndExchange(getDelayExchangeName(), getDelayQueueName(),
                    getDeadExchangeName(), getDeadQueueName(),
                    getInterval());
            return;
        }

        // 延时插件
        rabbitmqUtil.createDelayExchangeOfPlugin(getDeadExchangeName(), getDeadQueueName());
    }

    public void pushDataToMq(PushRetryDataDTO pushRetryDataDTO) {
        Long currentRetryCount = getCurrentRetryCount(pushRetryDataDTO.getUniqueId());
        Integer maxRetry = pushRetryDataDTO.getMaxRetry();
        if (ObjectUtil.isNotEmpty(currentRetryCount) && currentRetryCount >= maxRetry) {
            // 重试次数达到上限, 不再推送到mq延时
            log.info("收到重推数据, 唯一id: {}, 当前次数: {}, 推送接口: {}, 数据: {}, 重试已达最大次数: {}, 不再重推, 入库.",
                    pushRetryDataDTO.getUniqueId(), currentRetryCount, pushRetryDataDTO.getPushUrl(),
                    pushRetryDataDTO.getPushData(), maxRetry);
            // 入库
            doAfterReachMaxRetry(pushRetryDataDTO);
            // 删除redis 唯一id键
            deleteUniqueIdKey(pushRetryDataDTO.getUniqueId());
            return;
        }
        // 推送mq 等待重试
        RabbitmqUtil rabbitmqUtil = SpringUtil.getBean(RabbitmqUtil.class);
        if (dlx()) {
            rabbitmqUtil.sendDelayMessage(pushRetryDataDTO.getUniqueId(),
                    Duration.ofMillis(pushRetryDataDTO.getInterval()),
                    getDelayExchangeName(),
                    getDelayQueueName(),
                    pushRetryDataDTO);
        } else {
            rabbitmqUtil.sendDelayPluginMessage(pushRetryDataDTO.getUniqueId(),
                    Duration.ofMillis(pushRetryDataDTO.getInterval()),
                    getDeadExchangeName(),
                    getDeadQueueName(),
                    pushRetryDataDTO);
        }

        log.info("收到重推数据, 唯一id: {}, 当前次数: {}, 推送接口: {}, 数据: {}, 发送mq延时队列成功",
                pushRetryDataDTO.getUniqueId(), currentRetryCount, pushRetryDataDTO.getPushUrl(), pushRetryDataDTO.getPushData());
        // 次数+1
        increaseRetryCount(pushRetryDataDTO.getUniqueId());
    }

    public void retry(PushRetryDataDTO pushRetryDataDTO) {
        String pushUrl = pushRetryDataDTO.getPushUrl();
        String pushData = pushRetryDataDTO.getPushData();
        try (HttpResponse response = HttpRequest.post(pushRetryDataDTO.getPushUrl())
                .body(pushRetryDataDTO.getPushData())
                .timeout(getTimeout())
                .execute()) {
            String body = response.body();
            if (response.isOk()) {
                Boolean checkPushResult = checkPushResult(body);
                if (checkPushResult) {
                    // 重试成功, 删除redis
                    deleteUniqueIdKey(pushRetryDataDTO.getUniqueId());
                    return;
                }
                // 结果码不正确
                pushRetryDataDTO.setErrorMessage(body);
                doErrorCodeResult(pushRetryDataDTO);
                log.error("重试推送接口: {}, data: {}, 接口返回状态码不对: {}", pushUrl, pushData, body);
                return;
            }

            // http状态码不对时重推
            pushRetryDataDTO.setErrorMessage(body);
            pushDataToMq(pushRetryDataDTO);
            log.error("重试推送接口: {}, data: {}, 接口调用http状态码不对: {}, 数据: {}", pushUrl, pushData, response.getStatus(), body);

        } catch (Exception e) {
            // 异常时重推
            log.error("重试推送接口: {}, 数据: {}, 接口调用异常: {}", pushUrl, pushData, e.getMessage());
            pushRetryDataDTO.setErrorMessage(e.getMessage());
            pushDataToMq(pushRetryDataDTO);
        }

    }

    /**
     * 接口结果码不正确 如何处理 入库保存?
     *
     * @param pushRetryDataDTO 推送数据
     */
    protected abstract void doErrorCodeResult(PushRetryDataDTO pushRetryDataDTO);

    /**
     * 重试次数达到上限, 不再推送到mq延时, 后续操作
     */
    protected abstract void doAfterReachMaxRetry(PushRetryDataDTO pushRetryDataDTO);

    /**
     * 次数+1
     */
    public abstract Long increaseRetryCount(String uniqueId);

    /**
     * 获取当前重试次数
     */
    public abstract Long getCurrentRetryCount(String uniqueId);

    /**
     * 删除重试次数计数器
     */
    public abstract void deleteUniqueIdKey(String uniqueId);

    /**
     * 最大重试次数
     */
    public abstract Integer maxRetry();

    /**
     * 推送接口调用超时时间
     */
    public abstract Integer getTimeout();

    /**
     * 重试间隔
     */
    public abstract Duration getInterval();

    /**
     * 检查推送接口返回结果code是否合法
     *
     * @param body 接口返回消息
     * @return 是否合法
     */
    public abstract Boolean checkPushResult(String body);

    /**
     * 是否为私信队列+ttl 延时
     */
    public abstract Boolean dlx();

    public abstract String getDelayExchangeName();

    public abstract String getDelayQueueName();

    public abstract String getDeadExchangeName();

    public abstract String getDeadQueueName();

}
