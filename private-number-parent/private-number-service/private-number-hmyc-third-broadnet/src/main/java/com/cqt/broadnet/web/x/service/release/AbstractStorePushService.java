package com.cqt.broadnet.web.x.service.release;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.common.Result;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-04-12 14:57
 * 推送客户接口入口抽象类
 */
@Slf4j
public abstract class AbstractStorePushService {

    /**
     * 推送客户接口
     *
     * @param uniqueId 唯一id
     * @param object   推送数据对象
     * @param vccId    企业id
     * @param pushFlag 是否推送
     * @param pushUrl  推送url
     */
    public void pushToCustomer(String uniqueId, Object object, String vccId, Integer pushFlag, String pushUrl, Integer maxRetry, Duration interval) {
        if (ObjectUtil.isEmpty(pushFlag) || pushFlag == 0 || StringUtils.isEmpty(pushUrl)) {
            log.info("该企业: {}, url: {}, 配置不推送 {}，recordId: {}", vccId, pushUrl, type(), uniqueId);
            return;
        }
        String pushData = JSON.toJSONString(object);
        try (HttpResponse httpResponse = HttpRequest.post(pushUrl)
                .timeout(getPushTimeout())
                .body(pushData)
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                Result result = getObjectMapper().readValue(body, Result.class);
                if (result.getCode() == 0) {
                    log.info("企业: {}, recordId: {}, {}推送成功, 推送接口: {}, 推送数据: {}, 结果: {}",
                            vccId, uniqueId, type(), pushUrl, pushData, body);
                    return;
                }

                // 接口返回码不正常, 进入重推再判断
                log.info("企业: {}, recordId: {}, {}推送接口返回失败, 推送接口: {}, 推送数据: {}, 结果: {}",
                        vccId, uniqueId, type(), pushUrl, pushData, body);
            }

            // 接口返回httpStatus不正常, 重试
            pushDataToMq(buildPushRetryDataDTO(pushUrl, uniqueId, vccId, vccId, pushData, maxRetry, interval));
            log.error("企业: {}, recordId: {}, {}接口返回httpStatus不正常: {}, 推送接口: {}", vccId, uniqueId, type(), httpResponse.getStatus(), pushUrl);
        } catch (Exception e) {
            // 接口调用异常, 重试
            log.error("企业: {}, recordId: {}, {}推送异常, 推送接口: {}, , 推送数据: {}， 异常信息: {}",
                    vccId, uniqueId, type(), pushUrl, pushData, e.getMessage());
            pushDataToMq(buildPushRetryDataDTO(pushUrl, uniqueId, vccId, vccId, pushData, maxRetry, interval));
        }
    }

    private PushRetryDataDTO buildPushRetryDataDTO(String pushUrl, String uniqueId, String vccId, String vccName, String pushData, Integer maxRetry, Duration interval) {
        String uniqueIdKey = PrivateCacheUtil.getBroadNetCallBillRetryCountKey(type(), vccId, uniqueId);
        return PushRetryDataDTO.builder()
                .pushData(pushData)
                .pushUrl(pushUrl)
                .vccId(vccId)
                .vccName(vccName)
                .uniqueIdKey(uniqueIdKey)
                .uniqueId(uniqueId)
                .maxRetry(maxRetry)
                .interval(interval.toMillis())
                .build();
    }

    /**
     * 推送mq
     *
     * @param pushRetryDataDTO 推送数据
     */
    public abstract void pushDataToMq(PushRetryDataDTO pushRetryDataDTO);

    /**
     * 获取ObjectMapper对象
     *
     * @return ObjectMapper对象
     */
    public abstract ObjectMapper getObjectMapper();

    /**
     * 获取接口推送超时时间
     *
     * @return 超时时间
     */
    public abstract Integer getPushTimeout();

    /**
     * 推送类型
     *
     * @return 推送类型
     */
    public abstract String type();
}
