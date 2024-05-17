package com.cqt.hmyc.web.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.hmyc.config.TaobaoBindInfoQueryTestAxbConfig;
import com.cqt.hmyc.config.TaobaoBindInfoQueryTestAxeConfig;
import com.cqt.model.call.dto.StartCallRequest;
import com.cqt.model.call.vo.AlibabaAliqinAxbVendorCallControlResponse;
import com.cqt.model.call.vo.CallControlResponse;
import com.cqt.model.properties.TaobaoApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-02-06 16:09
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final TaobaoApiProperties taobaoApiProperties;

    @PostMapping("testAxb")
    public CallControlResponse testAxb(@RequestParam Map<String, Object> params) {
        log.info("模拟接口入参: {}", JSON.toJSONString(params));
        String start = Convert.toStr(params.get("start_call_request"));
        StartCallRequest startCallRequest = JSON.parseObject(start, StartCallRequest.class);
        TaobaoApiProperties.TestBind testBind = taobaoApiProperties.getTestBind();
        String axbNumber = testBind.getAxbNumber();
        String axeNumber = testBind.getAxeNumber();

        String secretNo = startCallRequest.getSecretNo();
        // AXB模式
        if (secretNo.equals(axbNumber)) {
            Map<String, CallControlResponse> axbCache = TaobaoBindInfoQueryTestAxbConfig.CACHE;
            return axbCache.get("default");
        }

        // AXE模式 分机号
        if (secretNo.equals(axeNumber)) {
            Map<String, CallControlResponse> axeCache = TaobaoBindInfoQueryTestAxeConfig.CACHE;
            CallControlResponse controlResponse = axeCache.get("default");
            // 分机号是否有
            String extension = startCallRequest.getExtension();
            if (StrUtil.isEmpty(extension)) {
                CallControlResponse response = JSON.parseObject(JSON.toJSONString(controlResponse), CallControlResponse.class);
                AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto controlRespDto = response.getAlibabaAliqinAxbVendorCallControlResponse()
                        .getResult()
                        .getControlRespDto();
                controlRespDto.setControlOperate("IVR");
                return response;
            }
            if (!taobaoApiProperties.getTestBind().getDigitInfo().equals(extension)) {
                CallControlResponse response2 = JSON.parseObject(JSON.toJSONString(controlResponse), CallControlResponse.class);
                AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto controlRespDto = response2.getAlibabaAliqinAxbVendorCallControlResponse()
                        .getResult()
                        .getControlRespDto();
                controlRespDto.setControlOperate("REJECT");
                return response2;
            }
            return controlResponse;
        }

        return null;
    }
}
