package com.cqt.cloud.api.push;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.common.Result;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.push.entity.PrivateStatusInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * @date 2022/3/3 9:33
 */
@Component
@FeignClient(name = ServiceNameConstant.PRIVATE_NUMBER_PUSH, path = "/private-push", fallbackFactory = BindPushFeignClientFallbackFactory.class)
public interface BindPushFeignClient {

    /**
     * AYB 绑定通知
     *
     * @param aybBindPushDTO AYb 绑定推送信息
     * @return result
     */
    @PostMapping("/aybBind/receiver")
    Result pushAybBind(AybBindPushDTO aybBindPushDTO);

    /**
     * 解绑推送
     *
     * @param unbindPushDTO 解绑推送信息
     * @return 结果
     */
    @PostMapping("/unbind/receiver")
    Result pushUnBind(UnbindPushDTO unbindPushDTO);

    /**
     * 调用push 第三方话单推送
     *
     * @param acrJson 话单推送消息
     * @return CdrResult
     */
    @PostMapping("/third/bill/receiver")
    CdrResult thirdBillReceiver(@RequestBody String acrJson);

    /**
     * 状态推送
     *
     * @param privateStatusInfo 状态信息
     * @return 结果
     */
    @GetMapping("/status/receiver")
    Result statusReceiver(@SpringQueryMap PrivateStatusInfo privateStatusInfo);
}
