package com.cqt.sms.feign;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.call.vo.TaobaoBindInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhengsuhao
 * @date 2023-02-16
 */
@Component
@FeignClient(name = ServiceNameConstant.PRIVATE_NUMBER_QINGHAI_ALI_HMYC, path = "/private-number-qinghai-ali-hmyc",
        fallbackFactory = BindInfoQueryFeignFallbackFactory.class, decode404 = true)
public interface BindInfoQueryFeign {

    @GetMapping(value = "/bind/get")
    TaobaoBindInfoVO getBindInfo(@SpringQueryMap BindInfoApiQuery bindInfoApiQuery);
}
