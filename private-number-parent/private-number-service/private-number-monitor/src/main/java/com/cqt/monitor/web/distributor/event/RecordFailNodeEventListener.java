package com.cqt.monitor.web.distributor.event;

import cn.hutool.core.date.DateUtil;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.monitor.cache.SbcDistributorMonitorConfigCache;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022-12-06 9:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordFailNodeEventListener implements ApplicationListener<RecordFailNodeEvent> {

    private final RedissonUtil redissonUtil;

    @Override
    public void onApplicationEvent(RecordFailNodeEvent event) {

        OperateTypeEnum operateTypeEnum = event.getOperateTypeEnum();
        String currentIp = SbcDistributorMonitorConfigCache.getServerIp();
        String disConnectNodeKey = PrivateCacheUtil.getDisConnectNodeKey(currentIp);
        switch (operateTypeEnum) {
            case DELETE:
                Boolean delKey = redissonUtil.delKey(disConnectNodeKey);
                log.info("disConnectNodeKey: {}, delKey: {}", disConnectNodeKey, delKey);
                break;
            case INSERT:
                Boolean setString = redissonUtil.setString(disConnectNodeKey, DateUtil.now());
                log.info("disConnectNodeKey: {}, setString: {}", disConnectNodeKey, setString);
                break;
            default:
                break;
        }
    }
}
