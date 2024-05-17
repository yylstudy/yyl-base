package com.cqt.push.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.model.common.Result;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.push.service.QingHaiAliPushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zhengsuhao
 * @date 2023/2/8
 */
@Api(tags = "青海阿里隐私号话单推送接口")
@RestController
@Slf4j
@RequestMapping("/push")
public class QingHaiAliPushController {

    @Autowired
    @Qualifier("qingHaiAliPushServiceImpl")
    private QingHaiAliPushService qingHaiAliPushService;

    @ApiOperation("青海移动-阿里通话推送通话结束")
    @RequestMapping(value = "call/release", method = {RequestMethod.GET, RequestMethod.POST})
    public CdrResult aliEndCallRequest(@RequestBody String acrJson) {
        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        AcrRecordOrg acr = jsonObject.toJavaObject(AcrRecordOrg.class);
        log.info("开始调用通话结束事件推送接口: {}", acrJson);
        qingHaiAliPushService.toAliEndCallRequest(acr, 0);
        return CdrResult.ok(acr.getAcrCallId());
    }


    @ApiOperation("青海移动-阿里呼叫事件推送")
    @RequestMapping(value = "call/event/type", method = {RequestMethod.GET, RequestMethod.POST})
    public Result aliCallStatusReceiver(@Validated PrivateStatusInfo privateStatusInfo) {
        log.info("接收{}通话状态事件 =>{}", privateStatusInfo.getEvent(), privateStatusInfo);
        if (privateStatusInfo.getEvent().equals(CallEventEnum.hangup.name())) {
            return Result.ok();
        }
        qingHaiAliPushService.toAliCallStatusReceiver(privateStatusInfo, 0);
        return Result.ok();

    }

    @RequestMapping(value = "test", method = {RequestMethod.GET, RequestMethod.POST})
    public String test(@RequestParam Map<String, Object> params) {
        log.info("模拟客户收到话单接口:" + JSON.toJSONString(params));
        String a = "{\n" +
                "    \"alibaba_aliqin_axb_vendor_push_call_release_response\":{\n" +
                "        \"result\":{\n" +
                "            \"message\":\"error msg\",\n" +
                "            \"module\":true,\n" +
                "            \"code\":\"OK\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        return a;
    }

}
