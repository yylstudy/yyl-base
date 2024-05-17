package com.cqt.push.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.push.properties.PushProperties;
import com.cqt.push.PrivatePushApplication;
import com.cqt.push.config.RabbitMqConfig;
import com.cqt.push.config.RedissonUtil;
import com.cqt.push.dao.BillMessageDao;
import com.cqt.push.dao.FailMessageDao;
import com.cqt.push.entity.*;
import com.cqt.push.enums.UrlTypeEnum;
import com.cqt.push.mapper.IPrivateCorpBusinessInfoService;
import com.cqt.push.mapper.PrivateCdrRepushMapper;
import com.cqt.push.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.push.mapper.PrivateCorpInfoMapper;
import com.cqt.push.properties.BillProperties;
import com.cqt.push.properties.DialTestProperties;
import com.cqt.push.rabbitmq.MqSender;
import com.cqt.push.rabbitmq.RabbitService;
import com.cqt.push.utils.AuthUtil;
import com.cqt.push.utils.MeituanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PushService {


    private final IPrivateCorpBusinessInfoService businessInfoService;

    private final MqSender mqSender;

    private final FailMessageDao failMessageDao;

    private final RedissonUtil redissonUtil;

    private final PrivateCdrRepushMapper repushMapper;

    private final RabbitService rabbitService;

    private final PrivateCorpInfoMapper privateCorpInfoMapper;

    private final PrivateCorpBusinessInfoMapper businessInfoMapper;

    private final BillProperties billProperties;

    private final DialTestProperties dialTestProperties;

    private final BillMessageDao billMessageDao;



    @Async("threadPoolTaskExecutor")
    public void pushBillStart(PrivateFailMessage failMessage) {
        log.info("开始执行话单推送");
        PrivateBillInfo billInfo = JSON.parseObject(failMessage.getBody(), PrivateBillInfo.class);
        String recordId = billInfo.getRecordId();
        String vccId = failMessage.getVccid();
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId, UrlTypeEnum.BILL);
        if (vccInfo.getCdrPushFlag() == 0) {
            log.info("该企业配置不推送话单，recordId=>{}", recordId);
            return;
        }

//        if (billInfo.getCallResult() == 99) {
//            log.debug("当前话单无绑定关系，不推送，recordId=>{}", recordId);
//            return;
//        }

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

    public void pushBillMeituan(PrivateFailMessage failMessage) {
        log.info("开始执行话单推送");
        Bill billInfo = JSON.parseObject(failMessage.getBody(), Bill.class);
        String recordId = billInfo.getRecordId();
        String vccId = failMessage.getVccid();
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId, UrlTypeEnum.BILL);
        if (vccInfo.getCdrPushFlag() == 0) {
            log.info("该企业配置不推送话单，recordId=>{}", recordId);
            return;
        }

//        if (billInfo.getCallResult() == 99) {
//            log.debug("当前话单无绑定关系，不推送，recordId=>{}", recordId);
//            return;
//        }

        // 签名生成
        if (billInfo.getSign() == null) {
            billInfo.setTs((int) System.currentTimeMillis());
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
        PrivateStatusInfo statusInfo = JSON.parseObject(failMessage.getBody(), PrivateStatusInfo.class);
        String recordId = statusInfo.getRecordId();
        String vccId = failMessage.getVccid();
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId, UrlTypeEnum.STATUS);
        if (vccInfo.getStatusPushFlag() == 0){
            log.info("该企业({})配置通话状态不推送，recordId=>{}",vccId,recordId);
            return;
        }
        // 签名生成
        if (statusInfo.getSign() == null) {
            statusInfo.setTs(System.currentTimeMillis());
            AuthUtil.addSignToInstance(statusInfo, vccInfo.getSecretKey());
        }
        log.info("推送的通话状态报文=>{}\n推送通话状态的地址=>{} ,若为空则不推送",
                JSON.toJSONString(statusInfo), vccInfo.getStatusPushUrl());
        if (StringUtils.isEmpty(vccInfo.getStatusPushUrl())) {
            return;
        }

        try (HttpResponse httpResponse = HttpRequest.post(vccInfo.getStatusPushUrl())
                .timeout(10000)
                .body(JSON.toJSONString(statusInfo))
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

    @Async("threadPoolTaskExecutor")
    public void pushUnbindStart(PrivateFailMessage failMessage) {
        log.info("开始执行解绑推送");
        UnbindPushDTO unbindPushDTO = JSON.parseObject(failMessage.getBody(), UnbindPushDTO.class);
        String bindId = unbindPushDTO.getBindId();
        String vccId = failMessage.getVccid();
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId, UrlTypeEnum.UNBIND);
        if (vccInfo.getUnBindPushFlag()==0){
            log.info("该企业配置解绑事件不推送，VCCID=>{}",vccId);
            return;
        }
        // 签名生成
        if (unbindPushDTO.getSign() == null) {
            unbindPushDTO.setTs(System.currentTimeMillis());
            AuthUtil.addSignToInstance(unbindPushDTO, vccInfo.getSecretKey());
        }
        log.info("推送的解绑事件报文=>{}\n推送解绑事件的地址=>{} ,若为空则不推送",
                JSON.toJSONString(unbindPushDTO), vccInfo.getUnBindPushUrl());
        if (StringUtils.isEmpty(vccInfo.getUnBindPushUrl())) {
            return;
        }

        try (HttpResponse httpResponse = HttpRequest.post(vccInfo.getUnBindPushUrl())
                .timeout(10000)
                .body(JSON.toJSONString(unbindPushDTO))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String message = resObject.getString("message");
                if (code == 0) {
                    log.info("解绑事件推送成功，重推次数=>{}，bindId=>{}\n返回的报文=>{}",
                            failMessage.getNum(), bindId, body);
                } else {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.error("解绑事件推送成功，但返回错误码，解绑事件入库，bindId=>{}，错误报文=>{}", bindId, body);
                }
                return;
            }
            int status = httpResponse.getStatus();
            String s = "响应码："+status;
            catchBillException(failMessage, s);


        } catch (Exception e) {
            catchPushException(failMessage, e.getMessage());
        }
    }

    @Async("threadPoolTaskExecutor")
    public void pushAybBindStart(PrivateFailMessage failMessage) {
        AybBindPushDTO aybBindPushDTO = JSON.parseObject(failMessage.getBody(), AybBindPushDTO.class);
        String bindId = aybBindPushDTO.getBindId();
        String vccId = failMessage.getVccid();
        PrivateCorpBusinessInfo vccInfo = getVccInfo(vccId, UrlTypeEnum.AYB);
        // 签名生成
        if (aybBindPushDTO.getSign() == null) {
            aybBindPushDTO.setTs(System.currentTimeMillis());
            AuthUtil.addSignToInstance(aybBindPushDTO, vccInfo.getSecretKey());
        }
        log.info("推送的ayb绑定报文=>{}\n推送ayb绑定的地址=>{} ,若为空则不推送",
                JSON.toJSONString(aybBindPushDTO), vccInfo.getAybBindPushUrl());
        if (StringUtils.isEmpty(vccInfo.getAybBindPushUrl())) {
            return;
        }

        try (HttpResponse httpResponse = HttpRequest.post(vccInfo.getAybBindPushUrl())
                .timeout(10000)
                .body(JSON.toJSONString(aybBindPushDTO))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String message = resObject.getString("message");
                if (code == 0) {
                    log.info("ayb绑定事件推送成功，重推次数=>{}，bindId=>{}\n返回的报文=>{}",
                            failMessage.getNum(), bindId, body);
                } else {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.error("ayb绑定事件推送成功，但返回错误码，ayb绑定事件入库，bindId=>{}，错误报文=>{}", bindId, body);
                }
            }else {
                int status = httpResponse.getStatus();
                String s = "响应码："+status;
                catchBillException(failMessage, s);
            }

        } catch (Exception e) {
            catchPushException(failMessage, e.getMessage());
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
                        log.info("入庫失敗:" + e1);
                    }
                }
            }else {
                log.info("重推次数为空，请检查配置");
            }
        }catch (Exception e1){
            log.error("重推异常:" + e1);
        }


    }


    public PrivateCorpBusinessInfo getVccInfo(String vccId, UrlTypeEnum pushUrl) {
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
                if (UrlTypeEnum.BILL.equals(pushUrl)) {
                    if (StringUtils.isEmpty(businessInfo.getBillPushUrl())) {
                        if (ObjectUtil.isEmpty(businessInfo1)) {
                            businessInfo1 = businessInfoService.getById(vccId);
                        }
                        assert businessInfo1 != null;
                        businessInfo.setBillPushUrl(businessInfo1.getBillPushUrl());
                        redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));

                    }
                }
                if (UrlTypeEnum.STATUS.equals(pushUrl)) {
                    if (StringUtils.isEmpty(businessInfo.getStatusPushUrl())) {
                        if (ObjectUtil.isEmpty(businessInfo1)) {
                            businessInfo1 = businessInfoService.getById(vccId);

                        }
                        assert businessInfo1 != null;
                        businessInfo.setStatusPushUrl(businessInfo1.getStatusPushUrl());
                        redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));
                    }
                }
                if (UrlTypeEnum.AYB.equals(pushUrl)) {
                    if (StringUtils.isEmpty(businessInfo.getAybBindPushUrl())) {
                        if (ObjectUtil.isEmpty(businessInfo1)) {
                            businessInfo1 = businessInfoService.getById(vccId);
                        }
                        assert businessInfo1 != null;
                        businessInfo.setAybBindPushUrl(businessInfo1.getAybBindPushUrl());
                        redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));
                    }
                }
                if (UrlTypeEnum.UNBIND.equals(pushUrl)) {
                    if (StringUtils.isEmpty(businessInfo.getUnBindPushUrl())) {
                        if (ObjectUtil.isEmpty(businessInfo1)) {
                            businessInfo1 = businessInfoService.getById(vccId);
                        }
                        assert businessInfo1 != null;
                        businessInfo.setUnBindPushUrl(businessInfo1.getUnBindPushUrl());
                        redissonUtil.setString(busKey, JSONUtil.toJsonStr(businessInfo));
                    }
                }
            }

            return businessInfo;
        } catch (Exception e) {
            return businessInfoService.getById(vccId);
        }


    }


    public CheckBill buildMtCheckBill(AcrRecordOrg acr) {
        CheckBill checkBill = new CheckBill();
        JSONObject key7 = JSONObject.parseObject((String) acr.getKey7());
        log.info("接收拨测话单报文=>{}", JSON.toJSONString(acr));
        String ts = key7.getString("ts");

        checkBill.setAppKey(billProperties.getAppKey());
//        checkBill.setAppId(billProperties.getAppId());
        checkBill.setTs(StringUtils.isEmpty(ts) ? 0 : Integer.parseInt(ts));
        checkBill.setSign(key7.getString("sign"));
        checkBill.setRecordId(acr.getUuId());
        checkBill.setRequestId(key7.getString("request_id"));
        String numType = key7.getString("num_type");
        // axb ax  ayb
        if ("AXB".equals(numType)) {
            checkBill.setServiceCode(10);
        } else if ("AXE".equals(numType)) {
            checkBill.setServiceCode(20);
        } else if (StringUtils.isEmpty(numType)) {
            checkBill.setServiceCode(10);
        } else {
            checkBill.setServiceCode(10);
        }
        checkBill.setAreaCode( "99".equals(acr.getReleaseCause()) ? "" : (String) acr.getKey2());
        checkBill.setTelCalling(key7.getString("phone_calling"));
        checkBill.setTelCalled(acr.getCalledNum());
        checkBill.setTelX(acr.getCalledDisplayNum());
        checkBill.setBeginTime(MeituanUtil.strToUnixTime(acr.getCallOutTime()));
        checkBill.setReleaseTime(MeituanUtil.strToUnixTime(acr.getStopCallTime()));
        int connectTime = MeituanUtil.strToUnixTime(acr.getStartCallTime());
        checkBill.setConnectTime(checkBill.getReleaseTime() == connectTime ? 0 : connectTime);
        checkBill.setAlertingTime(MeituanUtil.strToUnixTime((String) acr.getKey1()));
        checkBill.setCallDuration(Integer.parseInt(acr.getCalledDuration()));
        checkBill.setBillDuration(Integer.parseInt(acr.getCalledDuration()));
        checkBill.setCallResult(MeituanUtil.callResult(acr.getReleaseCause()));
        checkBill.setBillCode("");
        checkBill.setHasCost(checkBill.getBillDuration() > 0 ? 1 : 0);
        checkBill.setCallCost(0);

        return checkBill;
    }

    public void checkPush(CheckBill checkBill, String vccid) {
        BillMessage billMessage = new BillMessage();
        billMessage.setBill(JSON.toJSONString(checkBill));
        billMessage.setIp(PrivatePushApplication.ip);
        billMessage.setVccid(vccid);
        checkPushStart(billMessage);
    }

    public void checkPushStart(BillMessage billMessage) {
        int num = dialTestProperties.getNum();
        JSONObject jsonObject = JSON.parseObject(billMessage.getBill());
        CheckBill checkBill = jsonObject.toJavaObject(CheckBill.class);
        log.info("推送的拨测话单报文=>{}", JSON.toJSONString(checkBill));
        log.info("推送话单的地址=>{}", dialTestProperties.getCheckPushUrl());
        try (HttpResponse httpResponse = HttpRequest.post(dialTestProperties.getCheckPushUrl())
                .timeout(10000)
                .body(JSON.toJSONString(checkBill))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String message = resObject.getString("message");
                if (code == 0) {
                    log.info("拨测话单推送成功，重推次数=>{}，RecordId=>{}\n返回的报文=>{}",
                            billMessage.getNum(), checkBill.getRecordId(), body);
                } else {
                    billMessage.setErrMsg(message);
                    billMessage.setCreateTime(new Date());
                    billMessageDao.insert(billMessage);
                    log.error("拨测话单推送成功，但返回错误码，话单入库，错误报文=>{}", body);
                }
            }
        } catch (Exception e) {
            log.error("拨测话单推送请求超时或无响应，RecordId=>{}，错误信息:{}", checkBill.getRecordId(), e.getMessage());
            // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
            if (billMessage.getNum() < num) {
                billMessage.setNum(billMessage.getNum() + 1);
                mqSender.sendCheckLazy(JSON.toJSONString(billMessage),num);
                log.info("拨测话单重推未超过次数，再次推送延迟队列。当前重推次数=>{}，RecordId=>{}", billMessage.getNum(), checkBill.getRecordId());
            } else {
                // 超过重推次数入库不再重推
                billMessage.setErrMsg("拨测话单重推超过次数");
                billMessage.setCreateTime(new Date());
                billMessageDao.insert(billMessage);
                log.info("拨测话单重推超过次数，数据入库不再推送。已重推次数=>{}，RecordId=>{}", billMessage.getNum(), checkBill.getRecordId());
            }
        }
    }

    public void test(MultipartFile file) throws IOException {
        String nu = "{\"allocationFlag\":0,\"areaCode\":\"0591\",\"areaName\":\"福建-福州\",\"autoLocationUpdate\":0,\"businessType\":\"AXE\",\"createBy\":\"guojianyan\",\"createTime\":1697788068740,\"numType\":6,\"number\":\"13466301092\",\"poolType\":\"AXE\",\"state\":0,\"supplierId\":\"hdh\",\"supportSms\":0,\"updateBy\":\"guojianyan\",\"updateTime\":1697788068740,\"vccId\":\"6608\"}";
        String xml = IoUtil.read(file.getInputStream(), StandardCharsets.UTF_8);
        String[] split = xml.split("\n");
        for (String s : split) {
            String[] split1 = s.split("\\|");
            String s1 = split1[2];
            String mbe = nu.replace("13466301092",s1);
            redissonUtil.setString11(PrivateCacheUtil.getNumberInfo(s1),mbe);
            log.info("key:"+PrivateCacheUtil.getNumberInfo(s1));
            log.info("value:"+mbe);
        }
    }
}
