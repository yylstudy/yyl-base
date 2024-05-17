package com.cqt.monitor.web.distributor.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.future.TaskLoader;
import com.cqt.monitor.cache.SbcDistributorMonitorConfigCache;
import com.cqt.monitor.web.distributor.event.DingtalkAlarmEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:29
 * 检测as健康检测接口是否正常
 */
@Slf4j
@Component
public class CheckHealthUrlTaskLoaderImpl implements TaskLoader<Boolean, String> {

    @Override
    public Boolean load(String url) {
        Integer maxRetry = SbcDistributorMonitorConfigCache.getMaxRetry();
        int failCount = 0;
        String errorMsg = "";
        for (int i = 1; i <= maxRetry; i++) {
            try {
                String result = HttpUtil.get(url, SbcDistributorMonitorConfigCache.getTimeout());
                JSONObject jsonObject = JSON.parseObject(result);
                String status = jsonObject.getString("status");
                if ("UP".equals(status)) {
                    log.info("url: {}, UP", url);
                    return true;
                }
                errorMsg = result;
                log.error("CheckHealthUrl: {}, status down, result: {}", url, result);
            } catch (Exception e) {
                errorMsg = e.getMessage();
                log.error("CheckHealthUrl: {}, error: {}", url, e.getMessage());
            }
            failCount++;
        }
        if (failCount == maxRetry) {
            log.error("CheckHealthUrl: {}, 连续失败 {} 次", url, failCount);
            SpringUtil.publishEvent(new DingtalkAlarmEvent(this, getMessage(url, maxRetry, errorMsg)));
            return false;
        }
        return true;
    }

    private String getMessage(String url, int i, String result) {
        return StrUtil.format(" 健康检测接口: {}{} 连续失败 {} 次异常: {}", url, StrUtil.LF, i, result);
    }
}
