package com.cqt.sms.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: private-number-parent
 * @description: 测试类
 * @author: yy
 * @create: 2022-03-23 18:44
 **/

@RestController
@Slf4j
public class TestController {

    @ApiOperation("测试接口")
    @PostMapping("/test")
    public void smsReceiver(@RequestBody String message) {
        log.info ("测试接收报文:{}", message);
    }
}
