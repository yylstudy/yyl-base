package com.cqt.cloud.api.hmyc;

import com.cqt.cloud.constants.ServiceNameConstant;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * date 2022/5/12 13:44
 * 号码隐藏服务
 */
@FeignClient(contextId = "privateNumberHmyc",
        path = "/private-number",
        value = ServiceNameConstant.PRIVATE_NUMBER_HMYC_SERVICE,
        fallbackFactory = PrivateNumberHmycFallbackFactory.class)
public interface PrivateNumberHmycFeignClient {

    /**
     * 查询绑定关系接口(内部)
     *
     * @param bindInfoApiQuery 查询条件
     * @return 绑定关系结果
     * @see com.cqt.model.bind.vo.BindInfoApiVO
     */
    @PostMapping("api/v1/bind/getBindInfo")
    ResultVO<BindInfoApiVO> queryBindInfo(@RequestBody BindInfoApiQuery bindInfoApiQuery);

}
