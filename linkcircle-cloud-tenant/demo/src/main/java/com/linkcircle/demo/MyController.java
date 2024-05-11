package com.linkcircle.demo;

import com.linkcircle.basecom.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/23 17:24
 */
@Slf4j
@RestController
@Tag(name = "测试")
public class MyController {
    @Autowired
    private SystemFeignClient systemFeignClient;

    @Operation(summary="查询配置")
    @GetMapping("queryByKey")
    private void test1(){
        Result<SysConfig> result = systemFeignClient.queryByKey("111");
        log.info("result:{}",result);
    }
}
