package com.cqt.cloud.api.basesetting;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.common.MessageDTO;
import com.cqt.model.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * @date 2022/5/12 13:44
 */
@Component
@FeignClient(contextId = "baseSettingApi", path = "/base-setting-api", value = ServiceNameConstant.BASE_SETTING_SERVICE,
        fallbackFactory = BaseSettingFallbackFactory.class)
public interface BaseSettingFeignClient {

    /**
     * 发送消息 短信sms, 邮箱email
     *
     * @param messageDTO 消息
     * @return 成功
     */
    @PostMapping("message/send")
    Result sendMessage(@RequestBody MessageDTO messageDTO);

}
