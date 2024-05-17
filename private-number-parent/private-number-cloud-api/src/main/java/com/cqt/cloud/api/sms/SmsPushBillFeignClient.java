package com.cqt.cloud.api.sms;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.push.entity.CdrResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * @date 2022/3/3 16:19
 */
@Component
@FeignClient(name = ServiceNameConstant.PRIVATE_NUMBER_SMS, path = "/private-num-sms",contextId = "SmsPushBill", fallbackFactory = SmsPushBillFallbackFactory.class)
public interface SmsPushBillFeignClient {

    /**
     * 调用sms push
     *
     * @param acrJson
     * @return CdrResult
     */
    @PostMapping("/third/sms-receive")
    CdrResult smsThirdPush(@RequestBody String acrJson);

}
