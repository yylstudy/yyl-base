package com.cqt.cloud.api.basesetting;

import com.cqt.model.common.MessageDTO;
import com.cqt.model.common.Result;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2022/5/16 11:26
 */
@Slf4j
@Component
public class BaseSettingFallbackFactory implements FallbackFactory<BaseSettingFeignClient> {

    @Override
    public BaseSettingFeignClient create(Throwable throwable) {
        log.error("feign 调用异常: ", throwable);
        return new BaseSettingFeignClient() {
            @Override
            public Result sendMessage(MessageDTO messageDTO) {
                log.info("sendMessage: {}, error: ", messageDTO, throwable);
                return Result.fail();
            }

        };
    }
}
