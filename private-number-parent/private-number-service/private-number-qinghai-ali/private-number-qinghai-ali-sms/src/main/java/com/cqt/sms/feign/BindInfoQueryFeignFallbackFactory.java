package com.cqt.sms.feign;

import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.model.call.vo.TaobaoBindInfoVO;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Xienx
 * @since 2023-05-06 09:45
 */
@Slf4j
@Component
public class BindInfoQueryFeignFallbackFactory implements FallbackFactory<BindInfoQueryFeign> {
    @Override
    public BindInfoQueryFeign create(Throwable cause) {
        return bindInfoApiQuery -> {
            log.error("call private-number-qinghai-ali-hmyc api failed, param: {} , error: ", bindInfoApiQuery, cause);
            return TaobaoBindInfoVO.fail(ErrorCodeEnum.SYSTEM_ERROR.getCode(), cause.getMessage());
        };
    }
}
