package com.cqt.sms.controller;

import com.alibaba.fastjson.JSON;
import com.cqt.model.common.Result;
import com.cqt.sms.model.entity.SmsRequest;
import com.cqt.sms.service.SmsPushService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Description: 短信推送
 * @author: scott
 * @date: 2022年03月25日 14:47
 */
@Slf4j
@RestController
public class SmsPushController {

    private final SmsPushService smsPushService;

    @Autowired
    public SmsPushController(SmsPushService smsPushService) {
        this.smsPushService = smsPushService;
    }

    /**
     * 通用短信转发
     * @param smsRequest 短信内容
     */
    @ApiOperation("上行短信接收, 推送到企业接口")
    @PostMapping(value = "/sms-receive")
    public Result smsPush(@RequestBody SmsRequest smsRequest) {
        log.info("msgId=>{}, 接收到VCCIDSMS短信请求: {}", smsRequest.getHeader().getMessageId(), JSON.toJSONString(smsRequest));
        smsPushService.smsReceive(smsRequest);
        return Result.ok();
    }
}
