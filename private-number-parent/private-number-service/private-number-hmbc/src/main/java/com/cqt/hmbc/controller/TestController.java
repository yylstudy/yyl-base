package com.cqt.hmbc.controller;

import com.alibaba.fastjson.JSONObject;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 *
 * @author Xienx
 * @date 2023年02月09日 9:10
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Api(value = "测试接口", tags = "测试接口")
public class TestController {
    
    @ApiOperation(value = "测试POST接口-body参数")
    @PostMapping
    public Result testPostBody(@RequestBody JSONObject object) {
        log.info("接收到body参数: {}", object.toJSONString());
        return Result.ok();
    }

    @ApiOperation(value = "测试接口-param参数")
    @RequestMapping("/param")
    public Result testParam(JSONObject object) {
        log.info("接收到param参数: {}", object.toJSONString());
        return Result.ok();
    }
}
