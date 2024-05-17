package com.cqt.cloud.api.msrn;

import com.cqt.model.common.ResultT;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 位置更新Fallback
 *
 * @author Xienx
 * @date 2022年05月26日 11:18
 */
@Slf4j
@Component
public class LocationUpdatingFallbackFactory implements FallbackFactory<LocationUpdatingFeignClient> {

    @Override
    public LocationUpdatingFeignClient create(Throwable throwable) {
        log.error("[{}] remote instance [{}], remote api call exception: ", MDC.get("requestId"), MDC.get("remoteIp"), throwable);
        return locationUpdatingInfos -> ResultT.error("同步批量位置更新请求失败: " + throwable.getMessage());
    }
}
