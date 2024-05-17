package com.cqt.broadnet.web.axb.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.broadnet.common.model.axb.dto.*;
import com.cqt.broadnet.common.model.x.dto.SmsCheckDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.common.utils.FormatUtil;
import com.cqt.broadnet.web.axb.mapper.SmsMapper;
import com.cqt.broadnet.web.axb.rabbitmq.DelayProducer;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.broadnet.web.x.service.release.StatusBillStorePushService;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.common.enums.CdrTypeCodeEnum;
import com.cqt.common.enums.ResultCodeEnum;
import com.cqt.common.enums.ServiceCodeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.common.Result;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.sms.save.SmsRequest;
import com.cqt.model.sms.save.SmsRequestBody;
import com.cqt.model.sms.save.SmsRequestHeader;
import com.cqt.model.unicom.entity.MeituanSmsStatePush;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-05-26 11:47
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxbCallNotificationService {

    private final StatusBillStorePushService statusBillStorePushService;

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    private final AxbCallBillStorePushService axbCallBillStorePushService;

    private final ObjectMapper objectMapper;

    private final LocalOrLongService localOrLongService;

    private final DelayProducer delayProducer;

    private final RedissonUtil redissonUtil;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final SmsMapper smsMapper;


    public void start(AxbCallStartDTO startDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("axb start params: {}", objectMapper.writeValueAsString(startDTO));
        }
        startDTO.setBindId(privateCorpBusinessInfoService.getCqtBindId(startDTO.getBindId()));
        PrivateStatusInfo statusInfo = buildPrivateStatusInfo(startDTO);
        statusInfo.setCurrentTime(longToDate(startDTO.getCallTime()));
        if (log.isInfoEnabled()) {
            log.info("axb start PrivateStatusInfo: {}", objectMapper.writeValueAsString(statusInfo));
        }
        statusBillStorePushService.pushStatus(statusInfo);
    }

    public void noBind(AxbCallNoBindDTO noBindDTO) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("axb no bind params: {}", objectMapper.writeValueAsString(noBindDTO));
        }
        PrivateBillInfo privateBillInfo = getBillFromNoBind(noBindDTO);
        Callstat callstat = toCallstat(privateBillInfo);
        axbCallBillStorePushService.storeCallBill(privateBillInfo, callstat);
    }

    public void finish(AxbCallFinishDTO finishDTO) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("axb finish params: {}", objectMapper.writeValueAsString(finishDTO));
        }
        PrivateBillInfo privateBillInfo = getPrivateBillInfo(finishDTO);
        if (!finishDTO.getStartTime().equals(finishDTO.getFinishTime())&&getRecordFlag(finishDTO)==1){
            log.info("话单推送至mq等待录音关联|callId=>{}",finishDTO.getCallId());
            delayProducer.send(privateBillInfo);
            return;
        }
        log.info("通话时长为0或未开启录音，直接推送给客户，callID=>{}",finishDTO.getCallId());
        Callstat callstat = toCallstat(privateBillInfo);
        axbCallBillStorePushService.storeCallBill(privateBillInfo, callstat);
    }
    private Integer getRecordFlag(AxbCallFinishDTO finishDTO){
        String numberInfoKey = PrivateCacheUtil.getNumberInfo(clear86(finishDTO.getX()));
        String numberInfo = redissonUtil.getStringX(numberInfoKey);
        PrivateNumberInfo privateNumberInfo = JSONObject.parseObject(numberInfo, PrivateNumberInfo.class);

        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), clear86(finishDTO.getCallNo()), privateNumberInfo.getNumber());
        String bindInfo = redissonUtil.getString(bindInfoKey);
        PrivateBindInfoAxb bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxb.class);
        if (ObjectUtil.isNotEmpty(bindInfoVO) && bindInfoVO.getEnableRecord() != null){
            return bindInfoVO.getEnableRecord();
        }
        return 1;
    }

    public void record(AxbCallRecordDTO recordDTO) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("axb record params: {}", objectMapper.writeValueAsString(recordDTO));
        }
        String recordUrlKey = PrivateCacheUtil.getRecordUrlKey(recordDTO.getCallId());
        redissonUtil.setString(recordUrlKey,recordDTO.getRecordUrl(),30, TimeUnit.MINUTES);
    }
    public PrivateStatusInfo buildPrivateStatusInfo(AxbCallStartDTO startDTO) throws JsonProcessingException {
        PrivateStatusInfo statusInfo = new PrivateStatusInfo();
        statusInfo.setEvent(CallEventEnum.callin.name());
        statusInfo.setRecordId(startDTO.getCallId());
        // 需转化
        statusInfo.setBindId(privateCorpBusinessInfoService.getCqtBindId(startDTO.getBindId()));
        statusInfo.setCaller(FormatUtil.getNumber(startDTO.getCallNo()));
        statusInfo.setCalled(FormatUtil.getNumber(startDTO.getPeerNo()));
        statusInfo.setTelX(FormatUtil.getNumber(startDTO.getX()));
        statusInfo.setNumType("AXB");
        // 未提供
        statusInfo.setCallResult(1);
        statusInfo.setExt("");
        PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(clear86(startDTO.getX()));
        statusInfo.setVccId(businessInfoDTO.getVccId());
        statusInfo.setAppKey(businessInfoDTO.getVccId());
        return statusInfo;
    }

    private PrivateBillInfo getBillFromNoBind(AxbCallNoBindDTO noBindDTO){
        PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(clear86(noBindDTO.getX()));
        PrivateBillInfo privateBillInfo = new PrivateBillInfo();
        privateBillInfo.setTelA(clear86(noBindDTO.getCallNo()));
        privateBillInfo.setAppKey(businessInfoDTO.getVccId());
        privateBillInfo.setTelX(clear86(noBindDTO.getX()));
        privateBillInfo.setTelY(clear86(noBindDTO.getX()));
        privateBillInfo.setRecordId(StrUtil.uuid());
        privateBillInfo.setBeginTime(longToDate(noBindDTO.getCallTime()));
        privateBillInfo.setAreaCode(privateCorpBusinessInfoService.getAreaCode(ThirdUtils.getNumberUn86(noBindDTO.getX())));
        privateBillInfo.setCalloutTime(longToDate(noBindDTO.getCallTime()));
        HashMap<String, String> map = new HashMap<>();
        map.put("vccId",businessInfoDTO.getVccId());
        String s = JSONUtil.toJsonStr(map);
        privateBillInfo.setUserData(s);
        privateBillInfo.setServiceCode(ServiceCodeEnum.AXB.getCode());
        return privateBillInfo;
    }


    private PrivateBillInfo getPrivateBillInfo(AxbCallFinishDTO finishDTO) throws JsonProcessingException {
        String numberInfoKey = PrivateCacheUtil.getNumberInfo(clear86(finishDTO.getX()));
        String numberInfo = redissonUtil.getStringX(numberInfoKey);
        PrivateNumberInfo privateNumberInfo = JSONObject.parseObject(numberInfo, PrivateNumberInfo.class);
        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), clear86(finishDTO.getCallNo()), privateNumberInfo.getNumber());
        String bindInfo = redissonUtil.getString(bindInfoKey);
        String bindTime = null;
        String requestId = null;
        PrivateBillInfo privateBillInfo = new PrivateBillInfo();

        PrivateBindInfoAxb bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxb.class);
        if (ObjectUtil.isNotEmpty(bindInfoVO)){
            bindTime = DateUtil.format(bindInfoVO.getCreateTime(), ThirdConstant.yyyy_MM_dd_HH_mm_ss);
            requestId = bindInfoVO.getRequestId();
            if (bindInfoVO.getEnableRecord() != null){
                privateBillInfo.setRecordFlag(bindInfoVO.getEnableRecord());
                privateBillInfo.setRecordStartTime(bindInfoVO.getEnableRecord()==1?longToDate(finishDTO.getStartTime()):"");
            }

        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(clear86(finishDTO.getX()));
        privateBillInfo.setTelA(clear86(finishDTO.getCallNo()));
        privateBillInfo.setTelB(clear86(finishDTO.getPeerNo()));
        privateBillInfo.setBindTime(bindTime);
        privateBillInfo.setRequestId(requestId);
        privateBillInfo.setAppKey(businessInfoDTO.getVccId());
        privateBillInfo.setCalloutTime(longToDate(finishDTO.getCallTime()));
        privateBillInfo.setConnectTime(longToDate(finishDTO.getStartTime()));
        privateBillInfo.setAlertingTime(longToDate(finishDTO.getRingTime()));
        privateBillInfo.setReleaseTime(longToDate(finishDTO.getFinishTime()));
        privateBillInfo.setCallDuration((int) (finishDTO.getFinishTime()- finishDTO.getStartTime()));
        privateBillInfo.setCallResult(getCallResult(finishDTO.getFinishState()));
        privateBillInfo.setTelX(clear86(finishDTO.getX()));
        privateBillInfo.setTelY(clear86(finishDTO.getX()));
        privateBillInfo.setRecordId(finishDTO.getCallId());
        privateBillInfo.setBeginTime(longToDate(finishDTO.getCallTime()));
        privateBillInfo.setBindId(privateCorpBusinessInfoService.getCqtBindId(finishDTO.getBindId()));
        privateBillInfo.setAreaCode(privateCorpBusinessInfoService.getAreaCode(ThirdUtils.getNumberUn86(finishDTO.getX())));
        HashMap<String, String> map = new HashMap<>();
        map.put("vccId",businessInfoDTO.getVccId());
        String s = JSONUtil.toJsonStr(map);
        privateBillInfo.setUserData(s);
        privateBillInfo.setServiceCode(ServiceCodeEnum.AXB.getCode());

        return privateBillInfo;
    }

    public static String longToDate(long lo){
        Date date = new Date(lo*1000);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(date);
    }

    public static String clear86(String number){
        if (number.startsWith("86")){
            number = number.substring(2);
        }
        return number;
    }





    public Integer getCallResult(Integer finishState) {
        if (finishState == 1) {
            return ResultCodeEnum.twenty.getCode();
        } else if (finishState == 2) {
            return ResultCodeEnum.one.getCode();
        } else if (finishState == 3) {
            return ResultCodeEnum.two.getCode();
        } else if (finishState == 4) {
            return ResultCodeEnum.four.getCode();
        } else if (finishState == 5) {
            return ResultCodeEnum.six.getCode();
        } else if (finishState == 6) {
            return ResultCodeEnum.seven.getCode();
        } else if (finishState == 7) {
            return ResultCodeEnum.one.getCode();
        } else {
            return ResultCodeEnum.ninetyNine.getCode();
        }
    }

    public Callstat toCallstat(PrivateBillInfo billInfo) throws ParseException {
        String chargeType = localOrLongService.getChargeType(billInfo.getTelB(), billInfo.getTelX());
        ResultCodeEnum callResultCodeEnum = ThirdUtils.cnResultCode(billInfo.getCallResult());
        String paraMap = billInfo.getUserData();
        String businessId;
        //判断是否为json
        try {
            JSONObject jsonObject = JSONObject.parseObject(paraMap);
            businessId = jsonObject.getString("businessId");
        } catch (Exception e) {
            businessId = "";
        }
        return Callstat.builder()
                .streamnumber(ThirdUtils.strToDateFormat(billInfo.getCalloutTime()) + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16))
                .serviceid(org.apache.commons.lang.StringUtils.isBlank(businessId) ? "" : businessId)
                .servicekey("900007")
                .callersubgroup("")
                .calleesubgroup("")
                .callerpnp("")
                .calleepnp("")
                .msserver("")
                .areanumber("")
                .dtmfkey("")
                .recordPush("")
                .calltype("0")
                .callcost(0)
                .calledpartynumber(ThirdUtils.getNumberUn86(billInfo.getTelX()))
                .callingpartynumber(ThirdUtils.getNumberUn86(billInfo.getTelA()))
                .chargemode("0")
                .specificchargedpar(ThirdUtils.getNumberUn86(billInfo.getTelX()))
                .translatednumber(ThirdUtils.getNumberUn86(billInfo.getTelB()))
                .startdateandtime(ThirdUtils.strToDateFormat(billInfo.getBeginTime()))
                .stopdateandtime(ThirdUtils.strToDateFormat(StringUtil.isEmpty(billInfo.getReleaseTime()) ? billInfo.getBeginTime() : billInfo.getReleaseTime()))
                .duration(String.valueOf(billInfo.getCallDuration()))
                .chargeclass("102")
                .transparentparamet(billInfo.getBindId())
                .acrcallid(ThirdUtils.acrCallId(ThirdUtils.strToDateFormat(billInfo.getBeginTime())))
                .oricallednumber(ThirdUtils.getNumberUn86(billInfo.getTelA()))
                .oricallingnumber(ThirdUtils.getNumberUn86(billInfo.getTelB()))
                .reroute("1")
                .groupnumber(billInfo.getAppKey())
                .callcategory("1")
                .chargetype(chargeType)
                .acrtype("1")
                .videocallflag(ThirdUtils.videoCallFlag(billInfo.getRecordFileUrl(), billInfo.getCallDuration()))
                .forwardnumber(billInfo.getRecordId())
                .extforwardnumber(org.apache.commons.lang.StringUtils.isBlank(billInfo.getAlertingTime()) ? "" : ThirdUtils.strToDateFormat(billInfo.getAlertingTime()))
                .srfmsgid(org.apache.commons.lang.StringUtils.isBlank(billInfo.getRecordFileUrl()) ? "" : billInfo.getRecordFileUrl())
                .begintime(ThirdUtils.strToDateFormat(billInfo.getBeginTime()))
                .releasecause(String.valueOf(callResultCodeEnum.getCode()))
                .releasereason(callResultCodeEnum.getDesc())
                .key5(CdrTypeCodeEnum.supplier.getCode())
//                .userpin(chinanetPorperties.getChinaNetId())
                .bNumFail("")
                .key3(billInfo.getAreaCode())
                .key2(ThirdUtils.strToDateFormat(billInfo.getBeginTime()))
                .key1("")
                .key4("")
                .build();
    }


    public void smsBack(SmsBack smsBackDTO) {
        PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(clear86(smsBackDTO.getVirtualCalled()));
        String content = redissonUtil.getString("sms_content_" + smsBackDTO.getMsgIdentifier());
//        String smsNum = redissonUtil.getString("sms_num_" + smsBackDTO.getMsgIdentifier());
        String smsNum = smsBackDTO.getTotalCount();
        String numberInfoKey = PrivateCacheUtil.getNumberInfo(clear86(smsBackDTO.getVirtualCalled()));
        String numberInfo = redissonUtil.getStringX(numberInfoKey);
        PrivateNumberInfo privateNumberInfo = JSONObject.parseObject(numberInfo, PrivateNumberInfo.class);
        if (ObjectUtil.isEmpty(privateNumberInfo)){
            privateNumberInfo = new PrivateNumberInfo();
        }
        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), clear86(smsBackDTO.getCalling()), privateNumberInfo.getNumber());
        String bindInfo = redissonUtil.getString(bindInfoKey);
        PrivateBindInfoAxb bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxb.class);

        SmsRequest smsRequest = toSmsRequest(smsBackDTO,businessInfoDTO,content,smsNum,privateNumberInfo,bindInfoVO);
        //短信发送结果入库
        delayProducer.send(smsRequest);
        smsBackDTO.setContent(content);
        smsBackDTO.setTimeStamp(DateUtil.format(DateUtil.offsetHour(DateUtil.parseUTC(smsBackDTO.getTimeStamp()),8),ThirdConstant.yyyy_MM_dd_HH_mm_ss));
        smsBackDTO.setCalled(clear86(smsBackDTO.getCalled()));
        smsBackDTO.setVirtualCalled(clear86(smsBackDTO.getVirtualCalled()));
        smsBackDTO.setDisplayCalling(clear86(smsBackDTO.getDisplayCalling()));
        smsBackDTO.setCalling(clear86(smsBackDTO.getCalling()));
        smsMapper.insert(smsBackDTO);
        MeituanSmsStatePush meituanSmsStatePush = buildPushSms(smsBackDTO, businessInfoDTO, content, smsNum,privateNumberInfo,bindInfoVO);
        log.info("短信推送内容："+JSONObject.toJSONString(meituanSmsStatePush));
        //发送结果推送企业
        try (HttpResponse httpResponse = HttpRequest.post(privateNumberBindProperties.getSmsPushUrl())
                .timeout(privateNumberBindProperties.getPushTimeout())
                .body(JSONObject.toJSONString(meituanSmsStatePush))
                .execute()) {

            String body = httpResponse.body();
            log.info("url=>{}|短信推送结果=>{}",privateNumberBindProperties.getSmsPushUrl(),body);

        } catch (Exception e) {
            // 接口调用异常, 重试
            log.error("url=>{}|短信推送调用接口异常=>{}",privateNumberBindProperties.getSmsPushUrl(),e);
        }

    }

    private SmsRequest toSmsRequest(SmsBack smsBackDTO,PrivateCorpBusinessInfoDTO businessInfoDTO,String content,
                                    String smsNum,PrivateNumberInfo privateNumberInfo,
                                    PrivateBindInfoAxb bindInfoVO){

        SmsRequest smsRequest = new SmsRequest();
        SmsRequestHeader smsRequestHeader = new SmsRequestHeader();
        smsRequestHeader.setMessageId(smsBackDTO.getMsgIdentifier());
        smsRequestHeader.setStreamNumber(StrUtil.uuid());
        smsRequest.setHeader(smsRequestHeader);
        SmsRequestBody smsRequestBody = new SmsRequestBody();
        if (ObjectUtil.isNotEmpty(businessInfoDTO)){
            smsRequestBody.setVccId(businessInfoDTO.getVccId());
        }
        smsRequestBody.setCalledNumber(clear86(smsBackDTO.getCalled()));
        smsRequestBody.setCallerNumber(clear86(smsBackDTO.getCalling()));
        smsRequestBody.setAPhoneNumber(clear86(smsBackDTO.getCalling()));
        smsRequestBody.setInPhoneNumber(clear86(smsBackDTO.getVirtualCalled()));
        smsRequestBody.setBPhoneNumber(clear86(smsBackDTO.getCalled()));
        smsRequestBody.setOutPhoneNumber(clear86(smsBackDTO.getDisplayCalling()));
        if (ObjectUtil.isNotEmpty(bindInfoVO)){
            smsRequestBody.setBindId(bindInfoVO.getBindId());
        }

        smsRequestBody.setSupplierId(privateNumberInfo.getSupplierId());
        if ("Success".equals(smsBackDTO.getResult())){
            smsRequestBody.setFailReason("success");
            smsRequestBody.setFailCode("0");
        }else {
            smsRequestBody.setFailReason("failed");
            smsRequestBody.setFailCode("9999");
        }

        smsRequestBody.setInContent(content);
        smsRequestBody.setSendTime(DateUtil.format(DateUtil.offsetHour(DateUtil.parseUTC(smsBackDTO.getTimeStamp()),8),ThirdConstant.yyyy_MM_dd_HH_mm_ss));

        smsRequestBody.setSmsNumber(Integer.valueOf(smsNum));
        smsRequest.setBody(smsRequestBody);
        return smsRequest;
    }

    private MeituanSmsStatePush buildPushSms(SmsBack smsBack,PrivateCorpBusinessInfoDTO businessInfoDTO,
                                             String content,String smsNum, PrivateNumberInfo privateNumberInfo,
                                             PrivateBindInfoAxb bindInfoVO){

        MeituanSmsStatePush smsPush = new MeituanSmsStatePush();
        smsPush.setSmsId(smsBack.getMsgIdentifier());
        smsPush.setAreaCode(privateNumberInfo.getAreaCode());
        smsPush.setAppkey(businessInfoDTO.getVccId());
        smsPush.setTs(System.currentTimeMillis());
        if (ObjectUtil.isNotEmpty(bindInfoVO)){
            smsPush.setBindId(bindInfoVO.getBindId());
            smsPush.setRequestId(bindInfoVO.getRequestId());
        }
        smsPush.setSender(clear86(smsBack.getCalling()));
        smsPush.setSenderShow(clear86(smsBack.getCalling()));
        smsPush.setReceiver(clear86(smsBack.getCalled()));
        smsPush.setReceiverShow(clear86(smsBack.getCalled()));
        smsPush.setServiceCode(10);
        smsPush.setTransferTime(DateUtil.now());
        smsPush.setSmsContent(content);
        smsPush.setSmsNumber(Integer.valueOf(smsNum));
        smsPush.setSmsResult("Success".equals(smsBack.getResult()) ? 0 : 1);
        return smsPush;
    }

    public void test(){
        for (int i = 0; i < 5000; i++) {
            SmsBack smsBackDTO = new SmsBack();
            smsBackDTO.setContent("content");
            smsBackDTO.setTimeStamp(DateUtil.now());
            smsBackDTO.setCalled("18789378332");
            smsBackDTO.setVirtualCalled("218879127");
            smsBackDTO.setDisplayCalling("8362e891271");
            smsBackDTO.setCalling("128689127e12");
            smsBackDTO.setMsgIdentifier(UUID.randomUUID().toString());
            smsMapper.insert(smsBackDTO);
        }
    }

}

