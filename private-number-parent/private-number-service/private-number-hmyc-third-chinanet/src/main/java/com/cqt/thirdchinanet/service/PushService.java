package com.cqt.thirdchinanet.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.CdrTypeCodeEnum;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.common.enums.ResultCodeEnum;
import com.cqt.common.enums.SmsResultCodeEnum;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.common.Result;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.thirdchinanet.config.RabbitMqConfig;
import com.cqt.thirdchinanet.dao.FailMessageDao;
import com.cqt.thirdchinanet.entity.ChinanetStatusInfo;
import com.cqt.thirdchinanet.entity.PrivateCdrRepush;
import com.cqt.thirdchinanet.entity.PrivateCorpInfo;
import com.cqt.thirdchinanet.entity.sms.*;
import com.cqt.thirdchinanet.mapper.*;
import com.cqt.thirdchinanet.porperties.ChinanetPorperties;
import com.cqt.thirdchinanet.rabbitmq.MqSender;
import com.cqt.thirdchinanet.rabbitmq.RabbitService;
import com.cqt.thirdchinanet.utils.AuthUtil;
import com.cqt.thirdchinanet.utils.PushUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Service
@Slf4j
public class PushService {


    @Autowired
    private IPrivateCorpBusinessInfoService businessInfoService;

    @Autowired
    private MqSender mqSender;

    @Autowired
    private FailMessageDao failMessageDao;


    @Autowired
    private RedissonUtil redissonUtil;

    @Autowired
    private PrivateCdrRepushMapper repushMapper;

    @Autowired
    private PrivateNumberInfoMapper numberInfoMapper;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private PrivateCorpInfoMapper privateCorpInfoMapper;

    @Autowired
    private PrivateCorpBusinessInfoMapper businessInfoMapper;

    @Autowired
    private  LocalOrLongService localOrLongService;

    @Autowired
    private  ChinanetPorperties chinanetPorperties;



    public Result billHandle(PrivateBillInfo billInfo){
        if (StringUtils.isEmpty(billInfo.getAreaCode())){
            billInfo.setAreaCode(localOrLongService.checkMobilePhone(billInfo.getTelX()));
            if (StringUtils.isEmpty(billInfo.getAreaCode())){
                billInfo.setAreaCode("0000");
            }
        }
        PrivateFailMessage failMessage = PushUtil.buildBillMessage(billInfo);
        failMessage.setType(PushTypeEnum.BILL.name());
        String format = String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, billInfo.getTelX());

        String vccId = redissonUtil.getString(format);
        if (StringUtil.isEmpty(vccId)){
            PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(billInfo.getTelX());
            if (privateNumberInfo==null){
                log.info("x号码不存在归属vccId");
                return Result.fail(-1,"x号码不存在归属vccId");
            }
            vccId = privateNumberInfo.getVccId();
        }
        try {
            log.info("话单入库");
            Callstat callstat =toCallstat(billInfo, vccId);
            log.info("入库话单实体："+callstat);
            mqSender.send(JSONUtil.toJsonStr(callstat), ThirdConstant.ICCPCDRSAVEEXCHANGE,ThirdConstant.ICCPCDRSAVEROUTEKEY,0);
        }catch (Exception e){
            e.printStackTrace();
            log.error("发送消息到iccp_cdr_save_exchange异常");
        }

        pushBillStart(failMessage);
        return Result.ok();
    }

    @Async("threadPoolTaskExecutor")
    public void pushBillStart(PrivateFailMessage failMessage) {
        log.info("开始执行话单推送");
        PrivateBillInfo billInfo = JSON.parseObject(failMessage.getBody(), PrivateBillInfo.class);
        String recordId = billInfo.getRecordId();
        String format = String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, billInfo.getTelX());
        String vccId = redissonUtil.getString(format);
        if (StringUtil.isEmpty(vccId)){
            PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(billInfo.getTelX());
            vccId = privateNumberInfo.getVccId();
            if (StringUtils.isEmpty(vccId)){
                log.info("无法根据X号码查询到vccId："+billInfo.getTelX());
                return;
            }
        }
        failMessage.setVccid(vccId);
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId);
        if (vccInfo.getCdrPushFlag() == 0){
            log.info("该企业配置不推送话单，recordId=>{}", recordId);
            return;
        }

        // 签名生成
        if (billInfo.getSign() == null) {
            billInfo.setTs(System.currentTimeMillis());
            AuthUtil.addSignToInstance(billInfo, vccInfo.getSecretKey());
        }
        log.info("推送的话单报文=>{}\n推送话单的地址=>{}, 若为空则不推送",
                JSON.toJSONString(billInfo), vccInfo.getBillPushUrl());
        if (StringUtils.isEmpty(vccInfo.getBillPushUrl())) {
            return;
        }

        try (HttpResponse httpResponse = HttpRequest.post(vccInfo.getBillPushUrl())
                .timeout(10000)
                .body(JSON.toJSONString(billInfo))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String message = resObject.getString("message");
                if (code == 0) {
                    log.info("话单推送成功，重推次数=>{}，RecordId=>{}\n返回的报文=>{}",
                            failMessage.getNum(), billInfo.getRecordId(), body);
                } else {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.error("话单推送成功，但返回错误码，话单入库，RecordId=>{}，错误报文=>{}", recordId, body);
                }

                return;
            }
            int status = httpResponse.getStatus();
            String s = "响应码："+status;
            catchBillException(failMessage, s);

        } catch (Exception e) {
            catchBillException(failMessage, e.toString());
        }
    }

    @Async("threadPoolTaskExecutor")
    public void pushStatusStart(PrivateFailMessage failMessage) {
        log.info("开始执行通话状态推送");
        ChinanetStatusInfo statusInfo = JSON.parseObject(failMessage.getBody(), ChinanetStatusInfo.class);
        String recordId = statusInfo.getRecordId();
        String format = String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, statusInfo.getTelX());
        String vccId = redissonUtil.getString(format);
        if (StringUtil.isEmpty(vccId)){
            PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(statusInfo.getTelX());
            vccId = privateNumberInfo.getVccId();
            if (StringUtils.isEmpty(vccId)){
                log.info("无法根据X号码查询到vccId："+statusInfo.getTelX());
                return;
            }
        }
        PrivateStatusInfo privateStatusInfo = new PrivateStatusInfo();
        BeanUtils.copyProperties(statusInfo,privateStatusInfo);
        privateStatusInfo.setVccId(vccId);
        privateStatusInfo.setAppKey(vccId);
        failMessage.setVccid(vccId);
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId);
        if (vccInfo.getStatusPushFlag() == 0){
            log.info("该企业({})配置通话状态不推送，recordId=>{}",vccId,recordId);
            return;
        }
        // 签名生成
        if (privateStatusInfo.getSign() == null) {
            privateStatusInfo.setTs(System.currentTimeMillis());
            AuthUtil.addSignToInstance(privateStatusInfo, vccInfo.getSecretKey());
        }
        log.info("推送的通话状态报文=>{}\n推送通话状态的地址=>{} ,若为空则不推送",
                JSON.toJSONString(privateStatusInfo), vccInfo.getStatusPushUrl());
        if (StringUtils.isEmpty(vccInfo.getStatusPushUrl())) {
            return;
        }

        try (HttpResponse httpResponse = HttpRequest.post(vccInfo.getStatusPushUrl())
                .timeout(10000)
                .body(JSON.toJSONString(privateStatusInfo))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String message = resObject.getString("message");
                if (code == 0) {
                    log.info("通话状态推送成功，重推次数=>{}，RecordId=>{}\n返回的报文=>{}",
                            failMessage.getNum(), recordId, body);
                } else {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("通话状态推送成功，但返回错误码，通话状态入库，RecordId=>{}，错误报文=>{}", recordId, body);
                }
                return;
            }

            if(vccInfo.getStatusPushFlag()==1){
                int status = httpResponse.getStatus();
                String s = "响应码："+status;
                catchPushException(failMessage, s);
            }else {
                log.info("通话状态推送失败，不重试");
            }



        } catch (Exception e) {
            if(vccInfo.getStatusPushFlag()==1){
                catchPushException(failMessage, e.getMessage());
            }else {
                log.info("通话状态推送失败，不重试");
            }

        }
    }



    public void pushSmsBill(IccpSmsStatePush callstat){
        String format = String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, callstat.getReceiver_show());
        String vccId = redissonUtil.getString(format);
        if (StringUtil.isEmpty(vccId)){
            PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(callstat.getReceiver_show());
            vccId = privateNumberInfo.getVccId();
        }
        SmsRequest smsRequest = buildSmsRequeset(callstat, vccId);
        //入库
        mqSender.send(smsRequest, ThirdConstant.ICCPSMSCDRSAVEEXCHANGE,ThirdConstant.ICCPSMSCDRSAVEROUTEKEY,0);
        PrivateFailMessage smsStatePush = new PrivateFailMessage();
        callstat.setAppkey(vccId);
        smsStatePush.setId(StrUtil.uuid());
        smsStatePush.setVccid(vccId);
        smsStatePush.setType(PushTypeEnum.SMS.name());
        smsStatePush.setIp(PushUtil.getLocalIpStr());
        smsStatePush.setBody(JSONUtil.toJsonStr(callstat));
        smsThirdPush(smsStatePush);

    }

    @Async("threadPoolTaskExecutor")
    public void smsThirdPush(@RequestBody PrivateFailMessage smsStatePush) {
        IccpSmsStatePush smsRequest = JSON.parseObject(JSONUtil.toJsonStr(smsStatePush.getBody()), IccpSmsStatePush.class);
        log.info("开始执行短信话单推送: {}",smsRequest);
        String uuid= java.util.UUID.randomUUID().toString();
        String vccId=smsRequest.getAppkey();
        PrivateCorpBusinessInfo vccIdConfigInfo =getVccInfo(vccId);
        String smsPushUrl = vccIdConfigInfo.getSmsPushUrl();
        try {

            String json=JSONUtil.toJsonStr(smsRequest);
            TreeMap paramsMap = JSON.parseObject(json, TreeMap.class);
            //获取sign
            smsRequest.setSign (PushUtil.createSign (paramsMap,vccId,vccIdConfigInfo.getSecretKey ()));
            log.info("{}|推送报文：{}|url：{}", uuid, JSONUtil.toJsonStr(smsRequest), smsPushUrl);
            String potsResult = HttpUtil.post (smsPushUrl, JSONUtil.toJsonStr(smsRequest));
            log.info(uuid + "|状态推送结果：" + potsResult);
            MeiTuanResp meiTuanResp = JSONUtil.toBean(potsResult, MeiTuanResp.class);
            smsStatePush.setId(StrUtil.uuid());
            smsStatePush.setVccid(vccId);
            smsStatePush.setType("SMS");
            smsStatePush.setIp(PushUtil.getLocalIpStr());
            smsStatePush.setBody(JSONUtil.toJsonStr(smsRequest));
            if (!"success".equals(meiTuanResp.getMessage())) {
                //推送失败，写入mq
                log.info(uuid + "|状态推送失败，写入mq进行重推");
                catchPushException(smsStatePush,null);
            }
        } catch (Exception e) {
            log.error(uuid + "|状态推送异常：" + e);
            smsStatePush.setId(StrUtil.uuid());
            smsStatePush.setVccid(vccId);
            smsStatePush.setIp(PushUtil.getLocalIpStr());
            smsStatePush.setType("SMS");
            smsStatePush.setBody(JSONUtil.toJsonStr(smsRequest));
            catchPushException(smsStatePush,String.valueOf(e));
        }

    }

    private void catchPushException(PrivateFailMessage failMessage, String e) {
        try{
            log.info("推送请求超时或无响应，push_type=>{}，错误信息:{}", failMessage.getType(), e);
            // 队列创建
            String queueName = String.format(RabbitMqConfig.PRIVATE_PUSH_DELAY_QUEUE, failMessage.getVccid());
            rabbitService.createQueue(queueName);
            // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
            int currentNum = failMessage.getNum();
            String type = failMessage.getType();
            String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, failMessage.getVccid());
            Object o = redissonUtil.getObject(businessKey);
            //重推次数
            Integer rePush ;
            //重推间隔时间
            Integer rePushTime ;
            if (o!=null){
                PrivateCorpBusinessInfo privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            }else {
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id",failMessage.getVccid());
                PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoService.getOne(wrapper);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            }

            if (rePush!=null){
                if (currentNum < rePush) {
                    failMessage.setNum(currentNum + 1);
                    mqSender.sendLazy(JSON.toJSONString(failMessage), queueName, rePushTime);
                    log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}，push_type=>{}", currentNum, type);
                } else {
                    // 超过重推次数入库不再重推
                    failMessage.setErrMsg("重推超过次数");
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("重推超过次数，数据入库不再推送。已重推次数=>{}，push_type=>{}", currentNum, type);
                }
            }else {
                log.info("重推次数为空，请检查配置");
            }

        }catch (Exception e1){
            e1.printStackTrace();
            log.error("重推异常");
        }

    }

    private void catchBillException(PrivateFailMessage failMessage, String e) {
        try{
            log.info("推送请求超时或无响应，push_type=>{}，错误信息:{}", failMessage.getType(), e);
            // 队列创建
            String queueName = String.format(RabbitMqConfig.PRIVATE_PUSH_DELAY_QUEUE, failMessage.getVccid());
            rabbitService.createQueue(queueName);
            // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
            int currentNum = failMessage.getNum();
            String type = failMessage.getType();
            String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, failMessage.getVccid());
            Object o = redissonUtil.getObject(businessKey);
            //重推次数
            Integer rePush ;
            //重推间隔时间
            Integer rePushTime ;
            if (o!=null){
                PrivateCorpBusinessInfo privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            }else {
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id",failMessage.getVccid()).last("limit 1");
                PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoService.getOne(wrapper);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            }

            if (rePush!=null){
                if (currentNum < rePush) {
                    failMessage.setNum(currentNum + 1);
                    mqSender.sendLazy(JSON.toJSONString(failMessage), queueName, rePushTime);
                    log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}，push_type=>{}", currentNum, type);
                } else {
                    // 超过重推次数入库不再重推
                    failMessage.setErrMsg("重推超过次数");
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    try {

                        QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                        wrapper.eq("vcc_id",failMessage.getVccid()).last("limit 1");
                        PrivateCorpBusinessInfo businessInfo = businessInfoMapper.selectOne(wrapper);
                        PrivateCdrRepush privateCdrRepush = new PrivateCdrRepush();
                        privateCdrRepush.setCdrPushUrl(businessInfo.getBillPushUrl());
                        privateCdrRepush.setVccId(businessInfo.getVccId());
                        PrivateCorpInfo corpInfo = privateCorpInfoMapper.selectOne(new QueryWrapper<PrivateCorpInfo>().eq("vcc_id", businessInfo.getVccId()).last("limit 1"));
                        privateCdrRepush.setVccName(corpInfo.getVccName());
                        privateCdrRepush.setFailReason(failMessage.getErrMsg());
                        privateCdrRepush.setJsonStr(failMessage.getBody());
                        privateCdrRepush.setRepushFailTime(new Date());
                        repushMapper.insert(privateCdrRepush);
                        log.info("入庫成功");
                    }catch (Exception e1){
                        log.info("入庫失敗");
                        e1.printStackTrace();
                    }

                }
            }else {
                log.info("重推次数为空，请检查配置");
            }
        }catch (Exception e1){
            e1.printStackTrace();
            log.error("话单重推异常");
        }


    }




    private PrivateCorpBusinessInfo getVccInfo(String vccId) {
        //业务配置key
        String busKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccId);
        try {
            Object o1 = redissonUtil.getObject(busKey);
            PrivateCorpBusinessInfo businessInfo;
            if (o1==null){
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id", vccId);
                businessInfo = businessInfoService.getOne(wrapper);
            }else {
                businessInfo=JSONUtil.toBean(JSONUtil.toJsonStr(o1),PrivateCorpBusinessInfo.class);
            }
            return businessInfo;
        }catch (Exception e){
            QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("vcc_id", vccId);
            return businessInfoService.getOne(wrapper);
        }


    }





    private SmsRequest buildSmsRequeset(IccpSmsStatePush iccpSmsStatePush,String vccId){
        String uuid= UUID.randomUUID().toString().replaceAll("-", "");
        //获取 失败码
        SmsResultCodeEnum smsResultCodeEnum=ThirdUtils.smsResultCodeEnum(iccpSmsStatePush.getSms_result());
        SmsRequest sendSmsRequest = new SmsRequest();
        SmsRequestBody body = new SmsRequestBody();
        SmsRequestHeader header = new SmsRequestHeader();
        header.setStreamNumber(uuid);
        header.setMessageId(iccpSmsStatePush.getSms_id());
        body.setBindId(iccpSmsStatePush.getBind_id());
        body.setAPhoneNumber(ThirdUtils.getNumberUn86(iccpSmsStatePush.getSender()));
        body.setBPhoneNumber(ThirdUtils.getNumberUn86(iccpSmsStatePush.getReceiver()));
        body.setInPhoneNumber(ThirdUtils.getNumberUn86(iccpSmsStatePush.getSender_show()));
        body.setOutPhoneNumber(ThirdUtils.getNumberUn86(iccpSmsStatePush.getReceiver_show()));
        body.setVccId(vccId);
        body.setRequestTime(ThirdUtils.timeStampTranfer(iccpSmsStatePush.getTransfer_time(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss));
        body.setSendTime(ThirdUtils.timeStampTranfer(iccpSmsStatePush.getTransfer_time(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss));
        body.setFailCode(String.valueOf(smsResultCodeEnum.getCode()));
        body.setFailReason(smsResultCodeEnum.getDesc());
        body.setCallerNumber(ThirdUtils.getNumberUn86(iccpSmsStatePush.getSender()));
        body.setCalledNumber(ThirdUtils.getNumberUn86(iccpSmsStatePush.getReceiver()));
        body.setSmsNumber(iccpSmsStatePush.getSms_number());
        body.setSupplierId(chinanetPorperties.getChinaNetId());

        body.setInContent(org.apache.commons.lang.StringUtils.isBlank(iccpSmsStatePush.getSms_content())? "":iccpSmsStatePush.getSms_content());
        sendSmsRequest.setHeader(header);
        sendSmsRequest.setBody(body);

        return sendSmsRequest;

    }

    public Callstat toCallstat(PrivateBillInfo billInfo, String vccId) throws ParseException {
        String chargeType=localOrLongService.getChargeType(billInfo.getTelB(),billInfo.getTelX());
        ResultCodeEnum callResultCodeEnum= ThirdUtils.cnResultCode(billInfo.getCallResult());
        String paraMap= billInfo.getUserData();
        String businessId;
        //判断是否为json
        try {
            JSONObject jsonObject = JSONObject.parseObject(paraMap);
            businessId=jsonObject.getString("businessId");
        } catch (Exception e) {
            businessId="";
        }
        return Callstat.builder()
                .streamnumber(ThirdUtils.strToDateFormat(billInfo.getCalloutTime())+UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16))
                .serviceid(org.apache.commons.lang.StringUtils.isBlank(businessId) ? "" :businessId)
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
                .stopdateandtime(ThirdUtils.strToDateFormat(billInfo.getReleaseTime()))
                .duration(String.valueOf(billInfo.getCallDuration()))
                .chargeclass("102")
                .transparentparamet(billInfo.getBindId())
                .acrcallid(ThirdUtils.acrCallId(ThirdUtils.strToDateFormat(billInfo.getBeginTime())))
                .oricallednumber(ThirdUtils.getNumberUn86(billInfo.getTelA()))
                .oricallingnumber(ThirdUtils.getNumberUn86(billInfo.getTelB()))
                .reroute("1")
                .groupnumber(vccId)
                .callcategory("1")
                .chargetype(chargeType)
                .acrtype("1")
                .videocallflag(ThirdUtils.videoCallFlag(billInfo.getRecordFileUrl(),billInfo.getCallDuration()))
                .forwardnumber(billInfo.getRecordId())
                .extforwardnumber(org.apache.commons.lang.StringUtils.isBlank(billInfo.getAlertingTime()) ? "" :ThirdUtils.strToDateFormat(billInfo.getAlertingTime()))
                .srfmsgid(org.apache.commons.lang.StringUtils.isBlank(billInfo.getRecordFileUrl()) ? "" : billInfo.getRecordFileUrl())
                .begintime(ThirdUtils.strToDateFormat(billInfo.getBeginTime()))
                .releasecause(String.valueOf(callResultCodeEnum.getCode()))
                .releasereason(callResultCodeEnum.getDesc())
                .key5(CdrTypeCodeEnum.supplier.getCode())
                .userpin(chinanetPorperties.getChinaNetId())
                .bNumFail("")
                .key3(billInfo.getAreaCode())
                .key2(ThirdUtils.strToDateFormat(billInfo.getBeginTime()))
                .key1("")
                .key4("")
                .build();
    }
}
