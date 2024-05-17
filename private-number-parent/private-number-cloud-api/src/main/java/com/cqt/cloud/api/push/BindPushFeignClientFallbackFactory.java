package com.cqt.cloud.api.push;

import com.cqt.model.common.Result;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.push.entity.PrivateStatusInfo;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * @date 2022/3/3 9:34
 */
@Slf4j
@Component
public class BindPushFeignClientFallbackFactory implements FallbackFactory<BindPushFeignClient> {

    @Override
    public BindPushFeignClient create(Throwable cause) {
        log.error("bind push feign client fallback, error message: ", cause);
        return new BindPushFeignClient() {
            @Override
            public Result pushAybBind(AybBindPushDTO aybBindPushDTO) {
                return Result.fail(500, "推送ayb失败");
            }

            @Override
            public Result pushUnBind(UnbindPushDTO unbindPushDTO) {
                return Result.fail(500, "推送unbind失败");
            }

            @Override
            public CdrResult thirdBillReceiver(@RequestBody String acrJson) {
                return CdrResult.fail("调用第三方话单推送失败!");
            }

            @Override
            public Result statusReceiver(PrivateStatusInfo privateStatusInfo) {
                return Result.fail(500, "推送状态失败");
            }
        };
    }
}
