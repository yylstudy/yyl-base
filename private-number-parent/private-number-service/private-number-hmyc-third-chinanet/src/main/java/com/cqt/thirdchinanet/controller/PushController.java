package com.cqt.thirdchinanet.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.common.Result;

import com.cqt.model.push.entity.*;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.thirdchinanet.entity.ChinanetStatusInfo;
import com.cqt.thirdchinanet.entity.sms.IccpSmsStatePush;
import com.cqt.thirdchinanet.service.LocalOrLongService;
import com.cqt.thirdchinanet.service.PushService;
import com.cqt.thirdchinanet.utils.PushUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author hlx
 * @date 2021-09-14
 */
@RestController
@Slf4j
@RequestMapping("/api/v1")
public class PushController {

    @Autowired
    private PushService pushService;


    /**
     * 话单接收接口
     *
     */
    @PostMapping("/cdr/{appkey}")
    public Result billReceiver(@RequestBody PrivateBillInfo billInfo, @PathVariable("appkey") String appkey) {
        log.info("接收到话单："+billInfo);
        billInfo.setAppKey(appkey);
        return  pushService.billHandle(billInfo);
    }

    /**
     * 短信话单接收接口
     *
     */
    @PostMapping("/sms/{appkey}")
    public Result smsReceiver(@RequestBody IccpSmsStatePush callstat, @PathVariable("appkey") String appkey) {
        log.info("接收到短信话单："+callstat);

        pushService.pushSmsBill(callstat);
        return Result.ok();
    }

    /**
     * 通话状态接收接口
     *
     * @param privateStatusInfo 状态信息实体类
     * @return 结果
     */
    @PostMapping("/status/{appkey}")
    public Result statusReceiver(@RequestBody ChinanetStatusInfo privateStatusInfo) {
        log.info("接收{}通话状态事件 =>{}", privateStatusInfo.getEvent(), privateStatusInfo);

        if (privateStatusInfo.getEvent().equals(CallEventEnum.hangup.name())) {
            return Result.ok();
        }
        PrivateFailMessage failMessage = PushUtil.buildStatusMessage(privateStatusInfo);
        failMessage.setType(PushTypeEnum.STATUS.name());

        pushService.pushStatusStart(failMessage);
        return Result.ok();
    }





}
