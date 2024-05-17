package com.cqt.sdk.client.event.arrange;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.initialize.ApplicationInfoInitialize;
import com.cqt.base.util.CacheUtil;
import com.cqt.feign.sdk.SdkInterfaceFeignClient;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * @author linshiqiang
 * date:  2023-08-08 10:39
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class CallStopArrangeCancelEventListener implements ApplicationListener<CallStopArrangeCancelEvent> {

    private final RedissonUtil redissonUtil;

    private final SdkInterfaceFeignClient sdkInterfaceFeignClient;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final ServerProperties serverProperties;

    @Override
    public void onApplicationEvent(@Nonnull CallStopArrangeCancelEvent event) {
        try {
            String companyCode = event.getCompanyCode();
            String agentId = event.getAgentId();
            String arrangeTaskKey = CacheUtil.getArrangeTaskKey(companyCode, agentId);
            String serverIp = redissonUtil.get(arrangeTaskKey);
            if (StrUtil.isEmpty(serverIp)) {
                return;
            }
            if (serverIp.equals(ApplicationInfoInitialize.SERVER_IP)) {
                boolean cancel = ArrangeTaskCache.cancel(companyCode, agentId);
                log.info("[取消事后处理任务-local] 企业: {}, 坐席: {}, 结果: {}", companyCode, agentId, cancel);
                if (cancel) {
                    redissonUtil.delKey(arrangeTaskKey);
                }
                return;
            }

            // 调用feign
            String sdkServer = cloudCallCenterProperties.getDefaultConfig().getSdkServer();
            String contextPath = serverProperties.getServlet().getContextPath();
            String url = StrFormatter.format(sdkServer, serverIp, serverProperties.getPort(), contextPath);
            Boolean cancelled = sdkInterfaceFeignClient.cancelArrangeTask(new URI(url), companyCode, agentId);
            log.info("[取消事后处理任务-feign] 企业: {}, 坐席: {}, 结果: {}", companyCode, agentId, cancelled);
        } catch (Exception e) {
            log.error("[取消事后处理任务] 异常: ", e);
        }
    }
}
