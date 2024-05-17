package com.cqt.sms.controller;

import com.cqt.model.common.Result;
import com.cqt.model.unicom.entity.SmsRequest;
import com.cqt.sms.service.QingHaiAliSmsInterface;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhengsuhao
 * @date 2023/2/6
 */
@Api(tags = "青海阿里隐私号短信接口")
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class QingHaiAliSmsController {

    private final QingHaiAliSmsInterface qingHaiAliSmsInterface;


    @ApiOperation("青海移动-阿里短信处理")
    @PostMapping(value = "intercept")
    public Result smsHandle(@RequestBody SmsRequest smsRequest) {
        qingHaiAliSmsInterface.smsHandle(smsRequest);
        return Result.ok();
    }


}
