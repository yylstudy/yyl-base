package com.cqt.push.service.impl;

import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.CommonConstant;
import com.cqt.common.constants.HmbcConstants;
import com.cqt.common.util.TaobaoApiClient;
import com.cqt.model.call.dto.AlibabaAliqinAxbVendorExceptionNoSyncRequest;
import com.cqt.model.common.Result;
import com.cqt.model.hmbc.vo.HmbcResult;
import com.cqt.model.properties.TaobaoApiProperties;
import com.cqt.push.service.DialTestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

/**
 * DialTestResultServiceImpl
 *
 * @author Xienx
 * @date 2023年02月08日 15:17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialTestResultServiceImpl implements DialTestResultService {
    private final TaobaoApiClient taobaoApiClient;
    private final TaobaoApiProperties taobaoApiProperties;

    @Override
    public Result hmbcResult(HmbcResult hmbcResult, HttpServletResponse response) {
        log.info("接收到拨测服务推送拨测结果: {}", JSON.toJSONString(hmbcResult));
        Result result = Result.ok();

        // 组装报文
        AlibabaAliqinAxbVendorExceptionNoSyncRequest request = new AlibabaAliqinAxbVendorExceptionNoSyncRequest();
        request.setSecretNo(hmbcResult.getNumber());
        request.setExceptionMsg(hmbcResult.getReason());
        request.setStatus(hmbcResult.getState());
        request.setVendorKey(taobaoApiProperties.getVendorKey());
        // 根据阿里接口文档定义 1 是恢复可用, 这里需要转换
        if (HmbcConstants.DialTestState.RECOVERY.getCode().equals(hmbcResult.getState())) {
            request.setStatus(CommonConstant.COMMON_STATUS_1);
        }
        // 向阿里推送报文
        String url = taobaoApiProperties.getTest() ? taobaoApiProperties.getTestSyncUrl() : taobaoApiProperties.getRequestUrl();

        log.info("异常号码状态同步请求接口地址: {} ", url);
        try {
            taobaoApiClient.callApi(url, request.getApiMethodName(), request.getTextParams());
        } catch (Exception e) {
            log.error("请求taobao接口异常: ", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage(e.getMessage());
            result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return result;
    }
}
