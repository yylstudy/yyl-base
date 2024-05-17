package com.cqt.push.controller;

import com.alibaba.fastjson.JSON;
import com.cqt.model.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * TestController
 *
 * @author Xienx
 * @date 2023年02月08日 15:34
 */
@Slf4j
@RestController
public class TestController {

    @PostMapping("testSync")
    public Result testSync(@RequestParam Map<String, Object> params) {
        log.info("模拟接口入参: {}", JSON.toJSONString(params));
        return Result.ok();
    }
}
