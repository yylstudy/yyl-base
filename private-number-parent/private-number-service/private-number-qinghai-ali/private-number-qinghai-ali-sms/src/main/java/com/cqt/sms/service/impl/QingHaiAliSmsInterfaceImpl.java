package com.cqt.sms.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.TaobaoApiConstant;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.common.enums.SmsChannelEnum;
import com.cqt.common.util.TaobaoApiClient;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.call.vo.TaobaoBindInfoVO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.properties.TaobaoApiProperties;
import com.cqt.model.push.dto.EndCallRequest;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.vo.AlibabaAliqinAxbVendorPushCallReleaseResponse;
import com.cqt.model.push.vo.CallReleaseResponse;
import com.cqt.model.sms.dto.SmsInterceptRequest;
import com.cqt.model.sms.vo.AlibabaAliqinAxbVendorSmsInterceptResponse;
import com.cqt.model.sms.vo.SmsInterceptResponse;
import com.cqt.model.unicom.entity.SmsRequest;
import com.cqt.model.unicom.entity.SmsRequestBody;
import com.cqt.model.unicom.entity.SmsRequestHeader;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.sms.QinghaiAliSmsApp;
import com.cqt.sms.config.RabbitMqConfig;
import com.cqt.sms.config.SmsProperties;
import com.cqt.sms.feign.BindInfoQueryFeign;
import com.cqt.sms.mapper.PrivateFailMessageMapper;
import com.cqt.sms.rabbitmq.RabbitMqSender;
import com.cqt.sms.rabbitmq.RabbitMqService;
import com.cqt.sms.service.IPrivateCorpBusinessInfoService;
import com.cqt.sms.service.QingHaiAliSmsInterface;
import com.cqt.sms.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Optional;

/**
 * @author zhengsuhao
 * @date 2023/2/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QingHaiAliSmsInterfaceImpl implements QingHaiAliSmsInterface {
    //行短主叫号码
    private static final String INDUSTRY_SMS_NUMBER = "1069";
    //分机号分隔符
    private static final String EXTENSION_SEPARATOR = "#";
    //行业短信签名分隔符
    private static final String INDUSTRY_SMS_SIGNATURE_SEPARATOR = "】";


    private final RedissonUtil redissonUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SmsProperties smsProperties;
    private final RabbitMqSender rabbitMqSender;
    private final RabbitMqService rabbitMqService;
    private final TaobaoApiClient taobaoApiClient;
    private final BindInfoQueryFeign bindInfoQueryFeign;
    private final TaobaoApiProperties taobaoApiProperties;
    private final PrivateFailMessageMapper privateFailMessageMapper;
    private final IPrivateCorpBusinessInfoService businessInfoService;


    @Override
    public void smsHandle(SmsRequest smsRequest) {
        log.info("接收到VCCIDSMS短信内容: {}", JSON.toJSONString(smsRequest));
        //转换参数
        String messageId = smsRequest.getHeader().getMessageId();
        BindInfoApiQuery bindInfoApiQuery = smsToBindInfoApiQuery(smsRequest);
        //feign调用获取阿里sms参数
        TaobaoBindInfoVO taobaoBindInfoVO = bindInfoQueryFeign.getBindInfo(bindInfoApiQuery);
        log.info("{} | 查询阿里绑定关系, 请求参数: {}, 返回结果: {}", messageId, JSON.toJSONString(bindInfoApiQuery), JSON.toJSONString(taobaoBindInfoVO));
        if (!ErrorCodeEnum.OK.getCode().equals(taobaoBindInfoVO.getCode())) {
            log.info("{} | 查询绑定关系失败", messageId);
            return;
        }
        String smsChannel = taobaoBindInfoVO.getSmsChannel();
        log.info("{} |sms_channel: {}", messageId, smsChannel);
        //是否禁用短信 0：不禁用 1禁用
        smsRequest.getBody().setType(0);
        smsRequest.getBody().setBindId(taobaoBindInfoVO.getBindId());
        // 设置被叫
        smsRequest.getBody().setBPhoneNumber(taobaoBindInfoVO.getCalledNum());
        // 设置主叫
        smsRequest.getBody().setOutPhoneNumber(taobaoBindInfoVO.getCallNum());
        if (!taobaoBindInfoVO.getCallNum().startsWith("86")) {
            smsRequest.getBody().setOutPhoneNumber("86" + taobaoBindInfoVO.getCallNum());
        }
        // 短信计费条数
        smsRequest.getBody().setSmsNumber(StringUtil.getSmsNumber(smsRequest.getBody().getInContent()));
        // 正常短信下发
        if (SmsChannelEnum.SMS_NORMAL_SEND.toString().equals(smsChannel)) {
            log.info("{} | 青海移动短信接口:短信下发", messageId);
            boolean sendResult = smsSend(smsRequest);
            // 向阿里推送短信话单
            if (!sendResult) {
                // 如果短信没有发送成功, 那么短信条件就是0
                smsRequest.getBody().setSmsNumber(0);
            }
            generateEndCallRequest(smsRequest, 0);
            return;
        }
        // 短信托收
        if (SmsChannelEnum.SMS_INTERCEPT.toString().equals(smsChannel)) {
            log.info("{} | 青海移动短信接口:短信托收", messageId);
            //执行短信托收
            toAliSendSmsIntercept(smsRequest, 0);
            return;
        }
        // 执行短信拦截
        log.info("{} | 青海移动短信接口:丢弃", messageId);
    }


    public void generateEndCallRequest(SmsRequest smsRequest, Integer failNum) {
        Optional<CallReleaseResponse> callReleaseResponse = toAliSendSmsService(smsRequest);
        if (callReleaseResponse.isPresent()) {
            AlibabaAliqinAxbVendorPushCallReleaseResponse alibabaAliqinAxbVendorPushCallReleaseResponse = callReleaseResponse.get().getAlibabaAliqinAxbVendorPushCallReleaseResponse();
            AlibabaAliqinAxbVendorPushCallReleaseResponse.Response result = alibabaAliqinAxbVendorPushCallReleaseResponse.getResult();
            if (!TaobaoApiConstant.OK.equals(result.getCode())) {
                log.info("青海移动短信话单接口:返回结果不是OK：是{},MQ重推", result.getCode());
                resendAliService(smsRequest, failNum, result.getMessage(), "list");
                return;
            }
            log.info("青海移动短信话单发送成功：返回code：{}，message：{}", result.getCode(), result.getMessage());
            return;
        }
        log.info("青海移动短信话单发送失败,无返回结果,MQ重推");
        resendAliService(smsRequest, failNum, "青海移动短信话单发送失败,无返回结果", "list");
    }

    public void toAliSendSmsIntercept(SmsRequest smsRequest, Integer failNum) {
        Optional<SmsInterceptResponse> smsInterceptResponse = smsIntercept(smsRequest);
        if (smsInterceptResponse.isPresent()) {
            AlibabaAliqinAxbVendorSmsInterceptResponse alibabaAliqinAxbVendorSmsInterceptResponse = smsInterceptResponse.get().getAlibabaAliqinAxbVendorSmsInterceptResponse();
            AlibabaAliqinAxbVendorSmsInterceptResponse.Response result = alibabaAliqinAxbVendorSmsInterceptResponse.getResult();
            if (!TaobaoApiConstant.OK.equals(result.getCode())) {
                log.info("青海移动短信托收接口:返回结果不是OK：是{},MQ重推", result.getCode());
                resendAliService(smsRequest, failNum, result.getMessage(), "intercept");
                return;
            }
            log.info("青海移动短信托收接口：返回code：{}，message：{}", result.getCode(), result.getMessage());
            return;

        }
        log.info("青海移动短信托收接口:托收失败,无返回结果,MQ重推");
        resendAliService(smsRequest, failNum, "青海移动短信托收接口:托收失败,无返回结果", "intercept");

    }

    private boolean smsSend(SmsRequest smsRequest) {
        //调用vccidsms发送短信
        String messageId = smsRequest.getHeader().getMessageId();
        String msgRequest = JSON.toJSONString(smsRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(msgRequest, headers);
        //请求VCCIDSMS发短信
        try {
            log.info("{} |请求VCCIDSMS报文: {} | url: {}", messageId, msgRequest, smsProperties.getSendSmsUrl());
            String sendResult = restTemplate.postForObject(smsProperties.getSendSmsUrl(), formEntity, String.class);
            log.info("{} |请求VCCIDSMS结果: {}", messageId, sendResult);
            if (!StringUtils.isEmpty(sendResult) && sendResult.contains(TaobaoApiConstant.OK)) {
                log.info("{} |短信发送成功！", messageId);
                return true;
            }
            log.info("{} |短信发送失败！", messageId);
            return false;
        } catch (Exception e) {
            log.error("{} |短信发送异常:", messageId, e);
            return false;
        }
    }

    public Optional<SmsInterceptResponse> smsIntercept(SmsRequest smsRequest) {
        //短信入库
        //调用阿里托发接口发送短信
        SmsRequestBody body = smsRequest.getBody();
        SmsRequestHeader header = smsRequest.getHeader();
        SmsInterceptRequest request = new SmsInterceptRequest();
        //插入隐私号
        request.setSecretNo(body.getInPhoneNumber());
        //插入短信发送主叫号码
        request.setCallNo(body.getAPhoneNumber());
        //插入短信内容，请使用UCS2进行编码
        request.setSmsContent(UnicodeUtil.toUnicode(body.getInContent(), false));
        //插入短信时间撮
        request.setMtTime(DateUtil.now());
        //插入每次呼叫行为和短信行为的唯一ID
        request.setCallId(header.getMessageId());
        //插入对应阿里侧的绑定关系ID
        request.setSubsId(body.getBindId());
        //插入分配给供应商的KEY
        request.setVendorKey(taobaoApiProperties.getVendorKey());
        try {
            String url = taobaoApiProperties.getRequestUrl();
            // 测试接口
            if (taobaoApiProperties.getTestBind().getTest()) {
                url = taobaoApiProperties.getTestSmsUrl();
            }
            log.info("青海移动托收接口地址: {}", url);
            Optional<String> stringOptional = taobaoApiClient.callApi(url, request.getApiMethodName(), request.getTextParams(request));
            if (stringOptional.isPresent()) {
                String result = stringOptional.get();
                if (result.contains(TaobaoApiConstant.ERROR_RESPONSE)) {
                    log.error("青海移动托收接口返回error_response：{}", result);
                    return Optional.empty();
                }
                if (StrUtil.isEmpty(result)) {
                    log.error("青海移动托收接口返回空");
                    return Optional.empty();
                }
                // 结果json处理
                return Optional.of(objectMapper.readValue(result, SmsInterceptResponse.class));
            }
        } catch (Exception e) {
            log.error("接口taobao请求失败: ", e);
        }
        return Optional.empty();
    }


    private BindInfoApiQuery smsToBindInfoApiQuery(SmsRequest smsRequest) {
        BindInfoApiQuery bindInfoApiQuery = new BindInfoApiQuery();
        SmsRequestHeader header = smsRequest.getHeader();
        SmsRequestBody smsRequestBody = smsRequest.getBody();
        // 解析分机号

        //获取发短信号码
        String aNumber = smsRequestBody.getAPhoneNumber();
        if (aNumber.startsWith("86")) {
            aNumber = aNumber.substring(2);
            smsRequestBody.setAPhoneNumber(aNumber);
        }
        String content = smsRequestBody.getInContent();

        //普通AX短信内容：#分机号#+短信内容
        if (!aNumber.startsWith(INDUSTRY_SMS_NUMBER) && content.startsWith(EXTENSION_SEPARATOR)) {
            String extNum = content.split(EXTENSION_SEPARATOR)[1];
            smsRequestBody.setInContent(content.replaceAll(EXTENSION_SEPARATOR + extNum + EXTENSION_SEPARATOR, ""));
            bindInfoApiQuery.setDigitInfo(extNum);
        }
        //【美团外卖】#0123#短信内容 /#0123#【美团外卖】短信内容
        if (aNumber.startsWith(INDUSTRY_SMS_NUMBER) //主叫号码为1069
                && (content.contains(INDUSTRY_SMS_SIGNATURE_SEPARATOR + EXTENSION_SEPARATOR) || content.startsWith(EXTENSION_SEPARATOR)) && content.split(EXTENSION_SEPARATOR)[1].length() == 4 && StrUtil.isNumeric(content.split(EXTENSION_SEPARATOR)[1])) {
            String extNum = content.split(EXTENSION_SEPARATOR)[1];
            //下发的短信内容去除分机号
            smsRequestBody.setInContent(content.replaceAll(EXTENSION_SEPARATOR + extNum + EXTENSION_SEPARATOR, ""));
            bindInfoApiQuery.setDigitInfo(extNum);
        }

        bindInfoApiQuery.setCaller(smsRequestBody.getAPhoneNumber());
        bindInfoApiQuery.setCalled(smsRequestBody.getInPhoneNumber());
        bindInfoApiQuery.setCallId(header.getMessageId());
        bindInfoApiQuery.setVccId(smsRequestBody.getVccId());
        bindInfoApiQuery.setBehaviorType("SMS");
        return bindInfoApiQuery;
    }


    private void resendAliService(SmsRequest smsRequest, Integer failNum, String errorMessage, String type) {
        PrivateFailMessage privateFailMessage = new PrivateFailMessage();
        privateFailMessage.setIp(QinghaiAliSmsApp.ip);
        String statusJson = JSON.toJSONString(smsRequest);
        privateFailMessage.setBody(statusJson);
        privateFailMessage.setType(PushTypeEnum.SMS.name());
        privateFailMessage.setVccid(smsRequest.getBody().getVccId());
        privateFailMessage.setNum(failNum);
        // 队列创建
        String vccid = smsRequest.getBody().getVccId();
        String delayedQueueName;
        if ("list".equals(type)) {
            delayedQueueName = String.format(RabbitMqConfig.ALI_SMS_PUSH_DELAY_QUEUE, vccid);
        } else {
            delayedQueueName = String.format(RabbitMqConfig.ALI_SMS_INTERCEPT_DELAY_QUEUE, vccid);
        }
        rabbitMqService.createQueue(delayedQueueName);
        String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccid);
        int currentNum = privateFailMessage.getNum();
        Object o = redissonUtil.getObject(businessKey);
        //重推次数
        Integer rePush;
        //重推间隔时间
        Integer rePushTime;
        if (o != null) {
            PrivateCorpBusinessInfo privateCorpBusinessInfo = JSON.parseObject(JSON.toJSONString(o), PrivateCorpBusinessInfo.class);
            rePush = privateCorpBusinessInfo.getPushRetryNum();
            rePushTime = privateCorpBusinessInfo.getPushRetryMin();
        } else {
            QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("vcc_id", vccid).last("limit 1");
            PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoService.getOne(wrapper);
            rePush = privateCorpBusinessInfo.getPushRetryNum();
            rePushTime = privateCorpBusinessInfo.getPushRetryMin();
        }
        //如果重推次数不等于0
        if (rePush != null) {
            if (currentNum < rePush) {
                privateFailMessage.setNum(currentNum + 1);
                privateFailMessage.setType(type);
                rabbitMqSender.sendLazy(privateFailMessage, delayedQueueName, rePushTime);
                log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}", currentNum);
            } else {
                privateFailMessage.setErrMsg(errorMessage);
                privateFailMessage.setCreateTime(new Date());
                privateFailMessageMapper.insert(privateFailMessage);
                log.info("重推超过次数，当前重推次数=>{},不再重推", currentNum);
            }
        } else {
            log.info("重推次数为空，请检查配置");
        }
    }


    private Optional<CallReleaseResponse> toAliSendSmsService(SmsRequest smsRequest) {
        EndCallRequest endCallRequest = new EndCallRequest();
        SmsRequestBody body = smsRequest.getBody();
        SmsRequestHeader header = smsRequest.getHeader();
        Date sendTime;
        if (StrUtil.isNotEmpty(body.getRequestTime())) {
            sendTime = DateUtil.parse(body.getRequestTime(), DatePattern.PURE_DATETIME_PATTERN);
        } else {
            sendTime = new Date();
        }
        String date = DateUtil.format(sendTime, DatePattern.NORM_DATETIME_PATTERN);
        try {
            //转换实体
            endCallRequest.setReleaseCause(9999);
            endCallRequest.setCallId(header.getMessageId());
            endCallRequest.setRingTime(date);
            endCallRequest.setStartTime(date);
            endCallRequest.setSecretNo(body.getInPhoneNumber());
            endCallRequest.setCallOutTime(date);
            endCallRequest.setReleaseDir(0);
            endCallRequest.setReleaseTime(date);
            endCallRequest.setSubsId(body.getBindId());
            endCallRequest.setVendorKey(taobaoApiProperties.getVendorKey());
            endCallRequest.setSmsNumber(body.getSmsNumber());
            endCallRequest.setCallType("1");
            endCallRequest.setCallNo(body.getAPhoneNumber());
            endCallRequest.setCalledNo(body.getBPhoneNumber());
            //调青海阿里通话结束事件接口
            String url = taobaoApiProperties.getRequestUrl();
            // 测试接口
            if (taobaoApiProperties.getTestBind().getTest()) {
                url = taobaoApiProperties.getTestSmsUrl();
            }
            log.info("青海移动通话结束话单接口地址: {}", url);
            Optional<String> stringOptional = taobaoApiClient.callApi(url, endCallRequest.getApiMethodName(), endCallRequest.getTextParams(endCallRequest));
            if (stringOptional.isPresent()) {
                String result = stringOptional.get();
                if (result.contains(TaobaoApiConstant.ERROR_RESPONSE)) {
                    log.error("青海移动通话结束话单接口返回error_response：{}", result);
                    return Optional.empty();
                }
                if (StrUtil.isEmpty(result)) {
                    log.error("青海移动通话结束话单接口返回空");
                    return Optional.empty();
                }
                // 结果json处理
                return Optional.of(objectMapper.readValue(result, CallReleaseResponse.class));
            }
        } catch (Exception e) {
            log.error("青海移动通话结束话单接口调用失败", e);
            return Optional.empty();
        }
        return Optional.empty();
    }

}
