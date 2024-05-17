package com.cqt.unicom.feign;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.unicom.entity.MeituanSmsStatePush;
import io.swagger.annotations.Api;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "远程调用短信状态推送")
@Component
@FeignClient(name = ServiceNameConstant.PRIVATE_NUMBER_SMS, path = "/private-num-sms", contextId = "SmsPushBill")
public interface SmsPushFeignService {

    /**
     * feign远程调用短信状态推送
     *
     * @param meituanSmsStatePush
     * @return smsRequest
     */
    @PostMapping("/third/sms-receive")
    CdrResult smsPush(MeituanSmsStatePush meituanSmsStatePush);


}
