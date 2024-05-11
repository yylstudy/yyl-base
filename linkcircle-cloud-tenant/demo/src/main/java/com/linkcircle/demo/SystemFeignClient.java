package com.linkcircle.demo;

import com.linkcircle.basecom.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/23 17:24
 */
@FeignClient(value = "system",fallbackFactory = SystemFeignClientFallbackFactory.class)
public interface SystemFeignClient {
    @GetMapping("config/queryByKey")
    Result<SysConfig> queryByKey(@RequestParam String key);

}
