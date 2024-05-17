package com.cqt.cloud.api.msrn;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.common.ResultT;
import com.cqt.model.hmbc.dto.LocationUpdatingReq;
import com.cqt.model.hmbc.dto.LocationUpdatingRsp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 位置更新结果通知Feign
 *
 * @author Xienx
 * @version 2.0
 * @date 2022年05月24日 11:15
 */
@Component
@FeignClient(contextId = "locationUpdatingApi", path = "/iccp-msrn", value = ServiceNameConstant.ICCP_MSRN_SERVICE,
        fallbackFactory = LocationUpdatingFallbackFactory.class, decode404 = true)
public interface LocationUpdatingFeignClient {

    /**
     * 批量号码的位置更新
     *
     * @param locationUpdatingInfos 批量位置更新请求参数
     * @return Result<List < LocationUpdatingRsp>> 更新结果
     */
    @PutMapping(value = "/location-updating/batch-sync")
    ResultT<List<LocationUpdatingRsp>> locationUpdatingBatchSync(@Validated @RequestBody List<LocationUpdatingReq> locationUpdatingInfos);
}
