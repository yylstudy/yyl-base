package com.cqt.sms.controller;

import com.alibaba.fastjson.JSON;
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

    private static final String RELEASE_CALL = "alibaba.aliqin.axb.vendor.push.call.release";

    @PostMapping("testSms")
    public String testSync(@RequestParam Map<String, String> params) {
        log.info("模拟接口入参: {}", JSON.toJSONString(params));
        String method = params.get("method");
        if (RELEASE_CALL.equals(method)) {
            return "{\"alibaba_aliqin_axb_vendor_push_call_release_response\":{\"result\":{\"message\":\"error msg\",\"module\":true,\"code\":\"OK\"}}}";
        }
        return "{\"alibaba_aliqin_axb_vendor_sms_intercept_response\":{\"result\":{\"message\":\"err msg\",\"module\":true,\"code\":\"OK\"}}}";
    }
}
