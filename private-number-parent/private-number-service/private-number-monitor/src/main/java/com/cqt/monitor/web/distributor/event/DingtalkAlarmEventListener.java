package com.cqt.monitor.web.distributor.event;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.cloud.api.basesetting.BaseSettingFeignClient;
import com.cqt.common.enums.MessageTypeEnum;
import com.cqt.model.common.MessageDTO;
import com.cqt.model.common.Result;
import com.cqt.monitor.cache.SbcDistributorMonitorConfigCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022-12-05 14:29
 * 钉钉告警事件监听
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingtalkAlarmEventListener implements ApplicationListener<DingtalkAlarmEvent> {

    private final BaseSettingFeignClient baseSettingFeignClient;

    @SneakyThrows
    @Async("threadPool")
    @Override
    public void onApplicationEvent(DingtalkAlarmEvent event) {

        String title = "【中间号dis组切换监控告警】";
        String content = StrUtil.format("{}{} 告警时间: {}{} 当前设备: {}{} 告警内容: {}{}",
                title, StrUtil.LF,
                DateUtil.date(), StrUtil.LF,
                SbcDistributorMonitorConfigCache.getServerIp(), StrUtil.LF,
                StrUtil.LF, event.getMessage());

        MessageDTO messageDTO = MessageDTO.builder()
                .content(content)
                .type(MessageTypeEnum.dingding.name())
                .group(SbcDistributorMonitorConfigCache.getGroup())
                .operateType(title)
                .msgType("text")
                .title(title)
                .build();
        Result sendMessage = baseSettingFeignClient.sendMessage(messageDTO);
        if (log.isInfoEnabled()) {
            log.info("messageDTO: {}, result: {}", JSON.toJSONString(messageDTO), JSON.toJSONString(sendMessage));
        }
    }
}
