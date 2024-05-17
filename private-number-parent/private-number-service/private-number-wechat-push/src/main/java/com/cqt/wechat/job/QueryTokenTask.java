package com.cqt.wechat.job;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.wechat.config.RedissonUtil;
import com.cqt.wechat.properties.WechatPushProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author huweizhong
 * date  2023/2/23 15:19
 */

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class QueryTokenTask {

    private final RedissonUtil redissonUtil;

    private final WechatPushProperties pushProperties;

    @Scheduled(cron = "0 0 0/1 * * ? ")
    @PostConstruct
    public void getToken() {

        try (HttpResponse httpResponse = HttpRequest.get(pushProperties.getTokenUrl())
                .timeout(10000)
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                log.info("请求微信鉴权接口返回体：" + body);
                JSONObject resObject = JSONObject.parseObject(body);
                String token = resObject.getString(PrivateCacheConstant.WECHAT_TOKEN);
                if (StringUtils.isNotEmpty(token)) {
                    redissonUtil.setString(PrivateCacheConstant.WECHAT_TOKEN, token);
                }
            }
        } catch (Exception e) {
            log.info("请求微信接口获取token失败|url：{}|{}", pushProperties.getTokenUrl(), e.getMessage());
        }

    }

}
