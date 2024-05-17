package com.cqt.hmyc.web.dailtest.feign;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2021/10/11 14:19
 */
@Component
@Slf4j
public class CommandFeignFallbackFactory implements FallbackFactory<CommandFeign> {

    @Override
    public CommandFeign create(Throwable throwable) {
        log.error("feign调用异常信息: {}", throwable.getMessage());
        return message -> null;
    }
}
