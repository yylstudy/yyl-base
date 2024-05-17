package com.cqt.cloud.api.sms;

import com.cqt.model.push.entity.CdrResult;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * @date 2022/5/16 11:26
 */
@Slf4j
@Component
public class SmsPushBillFallbackFactory implements FallbackFactory<SmsPushBillFeignClient> {

    @Override
    public SmsPushBillFeignClient create(Throwable throwable) {
        log.error("feign 调用异常: ", throwable);
        return new SmsPushBillFeignClient() {

            @Override
            public CdrResult smsThirdPush(@RequestBody String acrJson){
                return CdrResult.fail("调用失败!");
           }

        };
    }
}
