package com.cqt.push.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.TaobaoApiConstant;
import com.cqt.common.enums.AliReleaseCauseEnum;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.common.enums.ReleasereasonEnum;
import com.cqt.common.util.TaobaoApiClient;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.properties.TaobaoApiProperties;
import com.cqt.model.push.dto.EndCallRequest;
import com.cqt.model.push.dto.EventCallRequest;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.push.vo.AlibabaAliqinAxbVendorPushCallEventResponse;
import com.cqt.model.push.vo.AlibabaAliqinAxbVendorPushCallReleaseResponse;
import com.cqt.model.push.vo.CallEventResponse;
import com.cqt.model.push.vo.CallReleaseResponse;
import com.cqt.push.QinghaiAliPushApp;
import com.cqt.push.config.RabbitMqConfig;
import com.cqt.push.mapper.FailMessageDao;
import com.cqt.push.rabbitmq.RabbitMqService;
import com.cqt.push.rabbitmq.RabbtiMqSender;
import com.cqt.push.service.IPrivateCorpBusinessInfoService;
import com.cqt.push.service.QingHaiAliPushService;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * @author zhengsuhao
 * @date 2023/2/8
 */
@Api(tags = "青海阿里推送通话结束接口：推送通话结束服务实现")
@Slf4j
@Service
@RequiredArgsConstructor
public class QingHaiAliPushServiceImpl implements QingHaiAliPushService {
    private final TaobaoApiClient taobaoApiClient;

    private final TaobaoApiProperties taobaoApiProperties;

    private final ObjectMapper objectMapper;

    private final RabbitMqService rabbitMqService;

    private final RabbtiMqSender rabbtiMqSender;

    private final RedissonUtil redissonUtil;

    private final IPrivateCorpBusinessInfoService businessInfoService;

    private final FailMessageDao failMessageDao;


    @Override
    public void toAliEndCallRequest(AcrRecordOrg acr, Integer num) {

        //内部话单转换为青海移动阿里通话结束话单
        EndCallRequest endCallRequest = convertAliEndCallRequest(acr);
        Optional<CallReleaseResponse> responseOptional = callPushService(endCallRequest, acr.getVccId());
        PrivateFailMessage failMessage = buildStatusMessage(acr);
        if (!responseOptional.isPresent()) {
            log.error("调用通话结束事件推送接口无返回结果，MQ重推,callId=>{}",acr.getAcrCallId());
            //重推话单
            failMessage.setNum(num);
            resendAliService(failMessage, "调用阿里通话结束事件推送接口无返回结果", "list");
            return;
        }
        AlibabaAliqinAxbVendorPushCallReleaseResponse callReleaseResponse = responseOptional.get().getAlibabaAliqinAxbVendorPushCallReleaseResponse();
        AlibabaAliqinAxbVendorPushCallReleaseResponse.Response result = callReleaseResponse.getResult();
        String code = result.getCode();
        if (!TaobaoApiConstant.OK.equals(code)) {
            // 返回不是OK
            log.error("返回结果code不是OK，MQ重推");
            //重推话单
            failMessage.setNum(num);
            resendAliService(failMessage, result.getMessage(), "list");
        }
        log.info("话单推送成功=>{}", acr.getAcrCallId());
    }

    @Override
    public void toAliCallStatusReceiver(PrivateStatusInfo privateStatusInfo, Integer num) {
        log.info("开始推送阿里通话事件服务接口: {}", JSON.toJSONString(privateStatusInfo));
        //转换为推送阿里服务实体
        EventCallRequest eventCallRequest = convertAliEventCallRequest(privateStatusInfo);
        Optional<CallEventResponse> callEventResponse = callStatusService(eventCallRequest, privateStatusInfo.getVccId());
        PrivateFailMessage failMessage = buildStatusMessage(privateStatusInfo);
        if (!callEventResponse.isPresent()) {
            log.error("调用阿里呼叫事件接口无返回结果，MQ重推");
            //重推话单
            failMessage.setNum(num);
            resendAliService(failMessage, "调用阿里呼叫事件接口无返回结果", "event");
            return;
        }
        AlibabaAliqinAxbVendorPushCallEventResponse callReleaseResponse = callEventResponse.get().getAlibabaAliqinAxbVendorPushCallEventResponse();
        AlibabaAliqinAxbVendorPushCallEventResponse.Response result = callReleaseResponse.getResult();
        String code = result.getCode();
        if (!TaobaoApiConstant.OK.equals(code)) {
            // 返回不是OK
            log.error("返回结果code不是OK，MQ重推");
            //重推话单
            failMessage.setNum(num);
            resendAliService(failMessage, result.getMessage(), "event");
            return;
        }
        log.info("通话状态推送成功=>{}", privateStatusInfo.getRecordId());
    }

    private PrivateFailMessage buildStatusMessage(PrivateStatusInfo privateStatusInfo) {
        PrivateFailMessage privateFailMessage = new PrivateFailMessage();
        privateFailMessage.setIp(QinghaiAliPushApp.ip);
        String statusJson = JSON.toJSONString(privateStatusInfo);
        privateFailMessage.setBody(statusJson);
        privateFailMessage.setType(PushTypeEnum.STATUS.name());
        privateFailMessage.setVccid(privateStatusInfo.getVccId());
        return privateFailMessage;
    }

    private PrivateFailMessage buildStatusMessage(AcrRecordOrg customerReceivesDataInfo) {
        PrivateFailMessage privateFailMessage = new PrivateFailMessage();
        privateFailMessage.setIp(QinghaiAliPushApp.ip);
        String statusJson = JSON.toJSONString(customerReceivesDataInfo);
        privateFailMessage.setBody(statusJson);
        privateFailMessage.setType(PushTypeEnum.BILL.name());
        privateFailMessage.setVccid(customerReceivesDataInfo.getVccId());
        return privateFailMessage;
    }


    private EndCallRequest convertAliEndCallRequest(AcrRecordOrg customerReceivesDataInfo) {
        //缺少、releasedir、两个字段
        EndCallRequest endCallRequest = new EndCallRequest();
        int integer = Integer.parseInt(customerReceivesDataInfo.getReleaseCause());
        AliReleaseCauseEnum aliReleaseCauseEnum = getReleasecause(integer);
        try {
            //呼叫释放原因
            endCallRequest.setReleaseCause(aliReleaseCauseEnum.getCode());
            //唯一呼叫ID，需要和转呼控制接口的call_id对应起来
            endCallRequest.setCallId(customerReceivesDataInfo.getUuId());
            //被叫响铃时间，如没有响铃时间，则等于call_out_time的时间；短信话单时，此值传短信接收时间
            endCallRequest.setRingTime(ThirdUtils.strToDateLong(StringUtil.isEmpty(String.valueOf(customerReceivesDataInfo.getKey1()))?customerReceivesDataInfo.getStartCallTime():String.valueOf(customerReceivesDataInfo.getKey1())));
            //被叫接听时间（通话计费开始时间），如未接通，则等于release_time的时间；短信话单时，此值传短信接收时间
            endCallRequest.setStartTime(1 == integer ? ThirdUtils.strToDateLong(StringUtil.isEmpty(customerReceivesDataInfo.getAbStartCallTime())?customerReceivesDataInfo.getStartCallTime():customerReceivesDataInfo.getAbStartCallTime()) : ThirdUtils.strToDateLong(StringUtil.isEmpty(customerReceivesDataInfo.getAbStopCallTime())?customerReceivesDataInfo.getStopCallTime():customerReceivesDataInfo.getAbStopCallTime()));
            //中间号
            endCallRequest.setSecretNo(customerReceivesDataInfo.getMiddleNumber());
            //呼叫被叫侧发起的时间，如未发起，则等于call_time的时间；短信话单时，此值传短信接收时间
            endCallRequest.setCallOutTime(ThirdUtils.strToDateLong(StringUtil.isEmpty(customerReceivesDataInfo.getCallOutTime())?customerReceivesDataInfo.getStartCallTime():customerReceivesDataInfo.getCallOutTime()));
            //0-平台释放 1-主叫释放 2-被叫释放；短信话单时，传0
            if (ReleasereasonEnum.CALL_CALLER_HANG_UP.toString().equals(customerReceivesDataInfo.getCallerRelReason()) && 1 == integer) {

                endCallRequest.setReleaseDir(2);
                endCallRequest.setCallResult("ANSWERED");

            } else if (ReleasereasonEnum.CALL_CALLER_CANCEL.toString().equals(customerReceivesDataInfo.getCallerRelReason()) && 9 == integer) {

                endCallRequest.setReleaseDir(1);
                endCallRequest.setCallResult("REJECT");


            } else if (9 == integer && customerReceivesDataInfo.getCalledRelReason().contains("normal_unspecified")){
                endCallRequest.setReleaseDir(2);
                endCallRequest.setCallResult("REJECT");

            } else if (9 == integer && "486:user_busy".equals(customerReceivesDataInfo.getCalledRelReason())){
                endCallRequest.setReleaseDir(2);
                endCallRequest.setCallResult("REJECT");

            }else if (ReleasereasonEnum.CALL_CALLED_HANG_UP.toString().equals(customerReceivesDataInfo.getCalledRelReason()) && 1 == integer) {

                endCallRequest.setReleaseDir(1);
                endCallRequest.setCallResult("ANSWERED");

            } else if (ReleasereasonEnum.CALL_CALLED_HANG_UP.toString().equals(customerReceivesDataInfo.getCalledRelReason()) && 9 == integer) {

                endCallRequest.setReleaseDir(1);
                endCallRequest.setCallResult("REJECT");

            }  else if (aliReleaseCauseEnum.getCode() == 21){
                endCallRequest.setCallResult("REJECT");
                endCallRequest.setReleaseDir(2);
            } else {
                endCallRequest.setReleaseDir(0);
            }

            if(6==integer){
                endCallRequest.setCallResult("INVALID_NUMBER");
            }else if (5==integer){
                endCallRequest.setCallResult("POWER_OFF");
            }else if (3==integer){
                endCallRequest.setCallResult("UNAVAILABLE");
            }else if (7==integer){
                endCallRequest.setCallResult("SUSPEND");
            }else if (2==integer||10==integer){
                endCallRequest.setCallResult("BUSY");
                endCallRequest.setReleaseCause(17);
            }

            //通话释放时间（通话计费结束时间）；短信话单时，此值传短信接收时间
            endCallRequest.setReleaseTime(ThirdUtils.strToDateLong(StringUtil.isEmpty(customerReceivesDataInfo.getAbStopCallTime())?customerReceivesDataInfo.getStopCallTime():customerReceivesDataInfo.getAbStopCallTime()));
            //分配给供应商Key
            endCallRequest.setVendorKey(taobaoApiProperties.getVendorKey());
            //被叫空闲振铃时间；如获取不到，则等于ring_time的时间
            endCallRequest.setFreeRingTime(endCallRequest.getRingTime());

            //呼叫结果
//            endCallRequest.setCallResult(integer == 1 ? "HANGUP" : "ALTERING");
            //话单类型 0-通话 1-短信
            endCallRequest.setCallType("0");
            //主叫号码
            endCallRequest.setCallNo(customerReceivesDataInfo.getCallInNum());
            //被叫号码
            endCallRequest.setCalledNo(customerReceivesDataInfo.getCalledNum());
            //绑定id
            endCallRequest.setSubsId(customerReceivesDataInfo.getMessageId());
            //分机号
            endCallRequest.setExtensionNo(customerReceivesDataInfo.getDtmfKey());
            JSONObject key7 = JSONObject.parseObject((String) customerReceivesDataInfo.getKey7());
            JSONObject key4 = JSONObject.parseObject((String) customerReceivesDataInfo.getKey4());
            String hangupIvr = (String) key7.get("dtmf_hangup_ivr");
            String ringRecordUrl = (String) key4.get("ringRecordUrl");
            String recordUrl = (String) key4.get("recordUrl");
            //录音下载URL,公网可以访问
            endCallRequest.setRecordUrl(recordUrl);
            endCallRequest.setRingingRecordUrl(ringRecordUrl);
            //未接通的通话不需要传录音
            if ("0".equals(customerReceivesDataInfo.getDuration())){
                endCallRequest.setRecordUrl(StrUtil.EMPTY);
            }else if (StringUtils.isEmpty(recordUrl)){
                //通话接通且录音url为空，说明不录音，通话前录音也不传
                endCallRequest.setRingingRecordUrl(StrUtil.EMPTY);
            }
//            if (StringUtils.isEmpty(endCallRequest.getRecordUrl())){
//                endCallRequest.setRingingRecordUrl(StrUtil.EMPTY);
//            }

            //dtmf按键
            endCallRequest.setEndCallIvrDtmf(hangupIvr);
        } catch (Exception e) {
            log.error("内部话单转换为青海移动阿里通话结束话单失败：", e);
        }
        return endCallRequest;
    }


    private EventCallRequest convertAliEventCallRequest(PrivateStatusInfo privateStatusInfo) {
        //呼叫开始
        EventCallRequest eventCallRequest = new EventCallRequest();
        try {
            //唯一的呼叫ID，最大可支持字符串长度256
            eventCallRequest.setCallId(privateStatusInfo.getRecordId());
            //被叫号码
            eventCallRequest.setCalledNo(privateStatusInfo.getCalled());
            //主叫号码
            eventCallRequest.setCallNo(privateStatusInfo.getCaller());
            //分机号
            if (StringUtil.isNotEmpty(privateStatusInfo.getExt())) {
                eventCallRequest.setExtensionNo(privateStatusInfo.getExt());
            }
            //振铃事件：ALERTING 摘机事件：PICKUP
            eventCallRequest.setEventType("ringing".equals(privateStatusInfo.getEvent()) ? "ALERTING" : "PICKUP");
            //绑定关系ID
            eventCallRequest.setSubsId(privateStatusInfo.getBindId());
            //供应商KEY
            eventCallRequest.setVendorKey(taobaoApiProperties.getVendorKey());
            //中间号码
            eventCallRequest.setSecretNo(privateStatusInfo.getTelX());
            //事件时间
            SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            eventCallRequest.setEventTime(df2.format(df1.parse(privateStatusInfo.getCurrentTime())));
            //被叫号显
            eventCallRequest.setCalledDisplayNo(privateStatusInfo.getCalledDisplayNo());
            //呼叫开始时间
            eventCallRequest.setCallTime(df2.format(df1.parse(privateStatusInfo.getCallTime())));
        } catch (Exception e) {
            log.error("通话状态转换为青海移动阿里通话事件失败：", e);
        }
        return eventCallRequest;

    }

//    public static void main(String[] args) throws ParseException {
//        String s = "20230811154646";
//        SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
//        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        String time2 = df2.format(df1.parse(s));
//        System.out.println(time2);
//    }

    private AliReleaseCauseEnum getReleasecause(Integer releasecause) {
        if (1 == releasecause) {
            return AliReleaseCauseEnum.thirty_one;
        }
        if (2 == releasecause) {
            return AliReleaseCauseEnum.seventeen;
        }
        if (3 == releasecause) {
            return AliReleaseCauseEnum.eighteen;
        }
        if (4 == releasecause) {
            return AliReleaseCauseEnum.nineteen;
        }
        if (6 == releasecause) {
            return AliReleaseCauseEnum.one;
        }
        if (7 == releasecause) {
            return AliReleaseCauseEnum.four;
        }
        if (8 == releasecause) {
            return AliReleaseCauseEnum.twenty_two;
        }
        return AliReleaseCauseEnum.twenty_one;
    }

    private Optional<CallReleaseResponse> callPushService(EndCallRequest endCallRequest, String vccId) {

        try {
            PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId);
            if (ObjectUtil.isEmpty(vccInfo)) {
                log.info("业务配置查询为空=>{}", vccId);
                return Optional.empty();
            }
            String url = vccInfo.getBillPushUrl();
            log.info("推送阿里通话结束服务: {}", url);
            Optional<String> stringOptional = taobaoApiClient.callApi(url, endCallRequest.getApiMethodName(), endCallRequest.getTextParams(endCallRequest));
            if (stringOptional.isPresent()) {
                String result = stringOptional.get();
                if (result.contains(TaobaoApiConstant.ERROR_RESPONSE)) {
                    log.error("阿里通话结束服务接口返回error_response:{}", result);
                    return Optional.empty();
                }
                if (StrUtil.isEmpty(result)) {
                    log.error("阿里通话结束服务接口返回空body=>{}",endCallRequest);
                    return Optional.empty();
                }
                // 结果json处理
                return Optional.of(objectMapper.readValue(result, CallReleaseResponse.class));
            }
        } catch (Exception e) {
            log.error("推送阿里通话结束服务异常>{},body=>{}", e,endCallRequest);
            return Optional.empty();
        }
        return Optional.empty();

    }

    private Optional<CallEventResponse> callStatusService(EventCallRequest eventCallRequest, String vccId) {
        try {
            PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId);
            if (ObjectUtil.isEmpty(vccInfo)) {
                log.info("业务配置查询为空=>{}", vccId);
                return Optional.empty();
            }
            String url = vccInfo.getStatusPushUrl();
            log.info("推送阿里呼叫事件服务: {}", url);
            Optional<String> stringOptional = taobaoApiClient.callApi(url, eventCallRequest.getApiMethodName(), eventCallRequest.getTextParams(eventCallRequest));
            if (stringOptional.isPresent()) {
                String result = stringOptional.get();
                if (result.contains(TaobaoApiConstant.ERROR_RESPONSE)) {
                    log.error("阿里呼叫事件接口返回error_response:{}", result);
                    return Optional.empty();
                }
                if (StrUtil.isEmpty(result)) {
                    log.error("阿里呼叫事件接口返回空,body=>{}",eventCallRequest);
                    return Optional.empty();
                }
                // 结果json处理
                return Optional.of(objectMapper.readValue(result, CallEventResponse.class));
            }
        } catch (Exception e) {
            log.error("推送阿里呼叫事件异常=>{},body=>{}", e,eventCallRequest);
            return Optional.empty();
        }
        return Optional.empty();
    }

    public PrivateCorpBusinessInfo getVccInfo(String vccId) {
        //业务配置key
        String busKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccId);
        try {
            Object o1 = redissonUtil.getObject(busKey);
            PrivateCorpBusinessInfo businessInfo;
            if (o1 == null) {
                businessInfo = businessInfoService.getById(vccId);
                redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));
            } else {
                businessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o1), PrivateCorpBusinessInfo.class);
                PrivateCorpBusinessInfo businessInfo1 = null;
                if (StringUtils.isEmpty(businessInfo.getPushRetryMin())) {
                    businessInfo1 = businessInfoService.getById(vccId);
                    if (ObjectUtil.isNotEmpty(businessInfo1)) {
                        businessInfo.setPushRetryMin(businessInfo1.getPushRetryMin());
                        redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));
                    }
                }
                if (StringUtils.isEmpty(businessInfo.getPushRetryNum())) {
                    if (ObjectUtil.isEmpty(businessInfo1)) {
                        businessInfo1 = businessInfoService.getById(vccId);

                    }
                    assert businessInfo1 != null;
                    businessInfo.setPushRetryNum(businessInfo1.getPushRetryNum());
                    redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));

                }
            }

            return businessInfo;
        } catch (Exception e) {
            return businessInfoService.getById(vccId);
        }
    }

    private void resendAliService(PrivateFailMessage failMessage, String errorMessage, String type) {
        // 队列创建
        String vccid = failMessage.getVccid();
        String delayedQueueName;
        try {
            String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccid);
            int currentNum = failMessage.getNum();
            Object o = redissonUtil.getObject(businessKey);
            //重推次数
            Integer rePush;
            //重推间隔时间
            Integer rePushTime;
            PrivateCorpBusinessInfo privateCorpBusinessInfo;
            if (o != null) {
                privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
            } else {
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id", vccid).last("limit 1");
                privateCorpBusinessInfo = businessInfoService.getOne(wrapper);
                redissonUtil.setString(businessKey, JSONUtil.toJsonStr(privateCorpBusinessInfo));
            }
            if ("list".equals(type)) {
                if (privateCorpBusinessInfo.getCdrPushFlag() == 0) {
                    log.info("该企业配置不推送话单，企业ID:{}", vccid);
                    return;
                }
                delayedQueueName = String.format(RabbitMqConfig.ALI_CALL_LIST_PUSH_DELAY_QUEUE, vccid);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            } else {
                if (privateCorpBusinessInfo.getStatusPushFlag() == 0) {
                    log.info("该企业配置不推送通话状态，企业ID:{}", vccid);
                    return;
                }
                delayedQueueName = String.format(RabbitMqConfig.ALI_CALL_EVENT_PUSH_DELAY_QUEUE, vccid);
                rePush = privateCorpBusinessInfo.getStatusRetryNum();
                rePushTime = privateCorpBusinessInfo.getStatusRetryMin();
            }
            rabbitMqService.createQueue(delayedQueueName);

            //如果重推次数不等于0
            if (rePush != null) {
                if (currentNum < rePush) {
                    failMessage.setNum(currentNum + 1);
                    failMessage.setType(type);
                    log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}", currentNum);
                    rabbtiMqSender.sendLazy(failMessage, delayedQueueName, rePushTime);
                } else {
                    failMessage.setErrMsg(errorMessage);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("重推超过次数，当前重推次数=>{},不再重推", currentNum);
                }
            } else {
                log.info("重推次数为空，请检查配置");
            }
        } catch (Exception e) {
            log.error("青海阿里重推异常=>{},failMessage=>{}", e,failMessage);
        }


    }

}
