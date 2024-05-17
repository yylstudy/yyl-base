package com.cqt.hmyc.web.bind.event;

import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-08-29 10:38
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AxeBindStatsEventListener implements ApplicationListener<AxeBindStatsEvent> {

    private final RedissonUtil redissonUtil;

    @Override
    public void onApplicationEvent(AxeBindStatsEvent event) {
        String vccId = event.getVccId();
        String areaCode = event.getAreaCode();
        OperateTypeEnum operateTypeEnum = event.getOperateTypeEnum();
        String axeBindStatsKey = PrivateCacheUtil.getAxeBindStatsKey(vccId, areaCode);
        if (OperateTypeEnum.INSERT.equals(operateTypeEnum)) {
            redissonUtil.increment(axeBindStatsKey);
            return;
        }
        redissonUtil.decrement(axeBindStatsKey);
    }
}
