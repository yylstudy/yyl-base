package com.cqt.sms.controller;

import cn.hutool.json.JSONUtil;
import com.cqt.model.numpool.entity.PrivateNumberPool;
import com.cqt.sms.model.entity.SmsRequest;
import com.cqt.sms.model.entity.SmsRequestBody;
import com.cqt.sms.service.SmsPushService;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@Slf4j
@RefreshScope
@RequiredArgsConstructor
public class SmsWhiteListController {

    @Autowired
    private  SmsPushService smsPushService;


    @NonNull
    @ApiOperation("匹配白名单词汇")
    @PostMapping(value = "/matchWhite")
    public String matchWhiteList(@RequestBody SmsRequest smsRequest){
        SmsRequestBody body = smsRequest.getBody();
        String systemTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        body.setRequestTime(systemTime);
        String inPhoneNumber = body.getInPhoneNumber();
        String vccId = body.getVccId();
        String inContent = body.getInContent();
        PrivateNumberPool privateNumberPool = smsPushService.findNumberPoolByNumber(inPhoneNumber);
        log.info(JSONUtil.toJsonStr(privateNumberPool));
        //判断X号码是否属于XSMS
        //匹配失败返回 1 ，成功返回 0,
        if(privateNumberPool==null){
            log.info("查询通用号码池为空");
            return "1";
        }
        if(!privateNumberPool.getNumType().equals("XSMS")){
            log.info("X号码不在XSMS号码池中");
            return "2";
        }else if(!smsPushService.smsCount(smsRequest)){
            log.info("X号码到达发送阈值或未匹配白名单");
            return "1";
            //判断X号码是否匹配白名单
        }else{
            return "0";
        }
    }



}
