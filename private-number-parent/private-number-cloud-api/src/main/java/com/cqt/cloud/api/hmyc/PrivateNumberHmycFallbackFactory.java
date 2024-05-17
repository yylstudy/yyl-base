package com.cqt.cloud.api.hmyc;

import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022-11-30 14:45
 */
@Slf4j
@Component
public class PrivateNumberHmycFallbackFactory implements FallbackFactory<PrivateNumberHmycFeignClient> {
    @Override
    public PrivateNumberHmycFeignClient create(Throwable throwable) {
        return new PrivateNumberHmycFeignClient() {
            @Override
            public ResultVO<BindInfoApiVO> queryBindInfo(BindInfoApiQuery bindInfoApiQuery) {
                log.error("hmyc, feign queryBindInfo, bindInfoApiQuery: {}, error：", bindInfoApiQuery, throwable);
                return ResultVO.fail(500, "error!");
            }
        };
    }
}
