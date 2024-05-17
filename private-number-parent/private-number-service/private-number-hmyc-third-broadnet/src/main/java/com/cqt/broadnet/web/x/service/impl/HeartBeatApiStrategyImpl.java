package com.cqt.broadnet.web.x.service.impl;

import com.cqt.broadnet.common.constants.ApiMethodConstant;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.web.x.service.ApiStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:05
 * 供应商心跳上报接口实现
 */
@Slf4j
@Service
public class HeartBeatApiStrategyImpl implements ApiStrategy {

    @Override
    public String getMethod() {
        return ApiMethodConstant.HEART_BEAT;
    }

    @Override
    public CallControlResponseVO execute(String jsonStr) {
        return CallControlResponseVO.ok();
    }
}
