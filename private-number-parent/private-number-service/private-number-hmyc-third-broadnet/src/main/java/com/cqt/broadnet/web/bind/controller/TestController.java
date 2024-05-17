package com.cqt.broadnet.web.bind.controller;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONObject;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.axb.job.UploadSmsJob;
import com.cqt.common.constants.SystemConstant;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.dto.AybBindDTO;
import com.cqt.model.common.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static com.cqt.common.constants.GatewayConstant.SUPPLIER_ID;

/**
 * @author huweizhong
 * date  2023/7/24 14:36
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(SystemConstant.BIND_URI )
@Api(tags = "广电行业定制接口API")
@Slf4j
public class TestController {

    private final UploadSmsJob uploadSmsJob;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final ObjectMapper objectMapper;

    @ApiOperation(value = "请求广电进行AYB绑定")
    @PostMapping("/binding/ayb")
    public Result bindingAyb(@RequestBody AybBindDTO bindingDTO) {
        String url = privateNumberBindProperties.getAybBindUrl();
        bindingDTO.setTs(String.valueOf(System.currentTimeMillis()));
        TreeMap<String, Object> treeMap = objectMapper.convertValue(bindingDTO, new TypeReference<TreeMap<String, Object>>() {
        });
        String sign = createSign(treeMap, privateNumberBindProperties.getSecretKey());
        bindingDTO.setSign(sign);
        try (HttpResponse response = HttpRequest.of(url)
                .method(Method.POST)
                .body(objectMapper.writeValueAsString(bindingDTO))
                .timeout(10000)
                .execute()) {
            log.info("请求广电接口: url: {}, 参数：{},response: {}", url, objectMapper.writeValueAsString(bindingDTO), response.body());
            if (response.isOk()) {
                return Result.ok();
            }
        } catch (Exception e) {
            log.error(" 请求广电接口: {}, 异常: ", url, e);
        }
        return Result.ok();
    }

    @ApiOperation(value = "请求广电进行AX绑定")
    @PostMapping("/binding/ax")
    public Result bindingAx(@RequestBody String bindingDTO) {
        String url = "";
        try (HttpResponse response = HttpRequest.of(url)
                .method(Method.POST)
                .body(bindingDTO)
                .timeout(10000)
                .execute()) {
            log.info("请求广电接口: url: {}, 参数：{},response: {}", url, bindingDTO,response.body());
            if (response.isOk()) {
                return Result.ok();
            }
        } catch (Exception e) {
            log.error(" 请求广电接口: {}, 异常: ", url, e);
        }
        return Result.ok();
    }

    @ApiOperation(value = "话单接收接口")
    @PostMapping("/cdr")
    public Result cdr(String bindingDTO) {
        log.info(bindingDTO);
        return Result.ok();
    }

    @ApiOperation(value = "定时任务")
    @PostMapping("/job")
    public Result job() throws Exception {
        uploadSmsJob.smsJob();
        return Result.ok();
    }

    public static String createSign(TreeMap<String, Object> params,  String secretKey) {
//        params.remove("appkey");
//        params.remove("sign");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE));
            }
        });
        paramList.add("secret=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }
}
