package com.linkcircle.demo;

import com.linkcircle.basecom.common.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/23 17:31
 */

public class SystemFeignClientFallbackFactory implements FallbackFactory<SystemFeignClient> {
    @Override
    public SystemFeignClient create(Throwable cause) {
        return new SystemFeignClient(){
            @Override
            public Result<SysConfig> queryByKey(String key) {
                return Result.error("查询异常");
            }
        };
    }
}
