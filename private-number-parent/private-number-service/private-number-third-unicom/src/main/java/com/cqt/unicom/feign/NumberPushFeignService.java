package com.cqt.unicom.feign;

import com.cqt.cloud.api.push.BindPushFeignClientFallbackFactory;
import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.common.Result;
import com.cqt.model.push.entity.PrivateStatusInfo;
import io.swagger.annotations.Api;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "远程调用通话状态推送")
@Component
@FeignClient(name = ServiceNameConstant.PRIVATE_NUMBER_PUSH, path = "/private-push", fallbackFactory = BindPushFeignClientFallbackFactory.class)
public interface NumberPushFeignService {

    /**
     * feign远程调用通话状态推送
     *
     * @param privateStatusInfo
     * @return Result
     */
    @ResponseBody
    @GetMapping(value = "/status/receiver", produces = "application/json")
    Result statusReceiver(@SpringQueryMap PrivateStatusInfo privateStatusInfo);

}
