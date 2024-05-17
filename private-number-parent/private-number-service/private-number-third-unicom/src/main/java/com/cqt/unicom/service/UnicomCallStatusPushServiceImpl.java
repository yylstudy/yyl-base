package com.cqt.unicom.service;

import cn.hutool.json.JSONObject;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.model.common.Result;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.unicom.dto.CallBusinessEventDTO;
import com.cqt.model.unicom.dto.CallConnectionStatusDTO;
import com.cqt.model.unicom.entity.UnicomCommonEnum;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.unicom.config.rabbitmq.RabbitMqSender;
import com.cqt.unicom.config.rabbitmq.UnicomRabbitMqConfig;
import com.cqt.unicom.feign.NumberPushFeignService;
import com.cqt.unicom.mapper.PrivateNumberInfoMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "联通集团总部(江苏)能力:通话状态推送服务实现")
@Slf4j
@Service
@RequiredArgsConstructor
public class UnicomCallStatusPushServiceImpl implements UnicomCallStatusPushService {


    private final RabbitMqSender rabbitMqSender;


    private final RedissonUtil redissonUtil;


    private final PrivateNumberInfoMapper numberInfoMapper;


    private final NumberPushFeignService numberPushFeignService;

    /**
     * @param callConnectionStatusDTO 联通集团总部(江苏)业务开始推送入参
     * @return PrivateStatusInfo
     */
    @Override
    @ApiOperation("集团报文转换客户接受报文服务实现")
    public PrivateStatusInfo getCustomerCallStatus(CallConnectionStatusDTO callConnectionStatusDTO) {
        log.info("江苏联通送集团状态(callout)原始入参：{}", callConnectionStatusDTO);
        PrivateStatusInfo privateStatusInfo = new PrivateStatusInfo();
        //插入为接入方分配的appkey
        privateStatusInfo.setAppKey("");
        try {
            //插入UNIXTIME时间戳，单位为ms
            long dateLag = System.currentTimeMillis();
            privateStatusInfo.setTs(dateLag);
            //插入当前状态发生时间
//            Date date = new Date();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//            String currentTime = simpleDateFormat.format(date);
//            privateStatusInfo.setCurrentTime(currentTime);
        } catch (Exception e) {
            log.error("操作异常: ", e);
        }
        privateStatusInfo.setCurrentTime(strDateFormat(callConnectionStatusDTO.getCallTime()));
        //插入签名
        privateStatusInfo.setSign("");
        //插入事件
        privateStatusInfo.setEvent("callout");
        //插入本次通话唯一标识
        privateStatusInfo.setRecordId(callConnectionStatusDTO.getCallId());
        //插入录音绑定ID
        privateStatusInfo.setBindId(callConnectionStatusDTO.getBindingId());
        //插入主叫
        privateStatusInfo.setCaller(callConnectionStatusDTO.getPhoneNumberA());
        //插入被叫
        privateStatusInfo.setCalled(callConnectionStatusDTO.getPhoneNumberB());
        //插入虚拟号
        privateStatusInfo.setTelX(callConnectionStatusDTO.getPhoneNumberX());
        //分机号
        if (StringUtil.isNotEmpty(callConnectionStatusDTO.getDgts())){
            privateStatusInfo.setExt(callConnectionStatusDTO.getDgts());
        }

        //插入针对tel_a号码
        privateStatusInfo.setCallType(11);
        //插入vccid
        String vccid = null;
        try {
            vccid = (String) redissonUtil.getObject(String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, privateStatusInfo.getTelX()));
            //如果找不到查数据库
            if (StringUtil.isEmpty(vccid)) {
                PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(callConnectionStatusDTO.getPhoneNumberX());
                if (privateNumberInfo != null) {
                    vccid = privateNumberInfo.getVccId();
                }
            }
        } catch (Exception e) {
            log.error("x: {}, 操作异常: ", callConnectionStatusDTO.getPhoneNumberX(), e);
        }
        privateStatusInfo.setVccId(vccid);
        //插入user_data
        if (!callConnectionStatusDTO.getAdditionalData().isEmpty()) {
            log.info("callout通话状态额外数据：{}", callConnectionStatusDTO.getAdditionalData());


            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject (callConnectionStatusDTO.getAdditionalData());
                privateStatusInfo.setUserData(jsonObject.getStr("userData"));
                if (StringUtil.isBlank(privateStatusInfo.getBindId())) {
                    privateStatusInfo.setBindId(jsonObject.getStr("bind_id"));
                }
            } catch (Exception e) {
                log.info("状态解析userDaa异常：{}", callConnectionStatusDTO.getAdditionalData(),e);
            }

        }
        return privateStatusInfo;
    }

    public static String strDateFormat(String dateStr) {
        DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sourceFormat.parse(dateStr);
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            return dateFormat.format(date);
        } catch (Exception e) {
            log.error("时间转换异常："+e);
            return dateStr;
        }
    }
    /**
     * @param callBusinessEventDTO 联通集团总部(江苏)业务事件通话状态推送入参
     * @return PrivateStatusInfo
     */
    @Override
    @ApiOperation("集团报文转换客户接受报文服务实现")
    public PrivateStatusInfo getCustomerCallStatus(CallBusinessEventDTO callBusinessEventDTO) {
        log.info("江苏联通送集团状态(answer)原始入参：{}", callBusinessEventDTO);
        PrivateStatusInfo privateStatusInfo = new PrivateStatusInfo();
        //插入为接入方分配的appkey
        privateStatusInfo.setAppKey("");
        try {
            //插入UNIXTIME时间戳，单位为ms

            privateStatusInfo.setTs(Long.parseLong(callBusinessEventDTO.getTimestamp()));
            //插入当前状态发生时间
            Date date = new Date(Long.parseLong(callBusinessEventDTO.getTimestamp()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentTime = simpleDateFormat.format(date);
            privateStatusInfo.setCurrentTime(currentTime);
        } catch (Exception e) {
            log.error("时间戳转换异常: ", e);
        }
        //插入签名
        privateStatusInfo.setSign("");
        //插入事件
        if (UnicomCommonEnum.ONE.getValue().equals(callBusinessEventDTO.getEvent())) {
            privateStatusInfo.setEvent("ringing");
        } else if (UnicomCommonEnum.TWO.getValue().equals(callBusinessEventDTO.getEvent())) {
            privateStatusInfo.setEvent("answer");
        }
        //插入本次通话唯一标识
        privateStatusInfo.setRecordId(callBusinessEventDTO.getCallId());
        //插入绑定ID
        privateStatusInfo.setBindId(callBusinessEventDTO.getBindingId());
        //插入主叫
        privateStatusInfo.setCaller(callBusinessEventDTO.getPhoneNumberA());
        //插入被叫
        privateStatusInfo.setCalled(callBusinessEventDTO.getPhoneNumberB());
        //分机号
        if (StringUtil.isNotEmpty(callBusinessEventDTO.getExtensionNumber())){
            privateStatusInfo.setExt(callBusinessEventDTO.getExtensionNumber());
        }
        //插入虚拟号
        privateStatusInfo.setTelX(callBusinessEventDTO.getPhoneNumberX());
        privateStatusInfo.setCallType(11);
        //插入vccid
        String vccid = null;
        try {
            vccid = (String) redissonUtil.getObject(String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, privateStatusInfo.getTelX()));
            //如果找不到查数据库
            if (StringUtil.isEmpty(vccid)) {
                PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(callBusinessEventDTO.getPhoneNumberX());
                vccid = privateNumberInfo.getVccId();
            }
        } catch (Exception e) {
            log.error("获取vccid操作异常: ", e);
        }
        privateStatusInfo.setVccId(vccid);
        //插入user_data
        log.info(privateStatusInfo.getEvent() + "通话状态额外数据：{}", callBusinessEventDTO.getData());
        if (callBusinessEventDTO.getData() != null) {
            JSONObject jsonObject = new JSONObject(callBusinessEventDTO.getData());
            privateStatusInfo.setUserData(jsonObject.getStr("userData"));
            if (StringUtil.isBlank(privateStatusInfo.getBindId())) {
                privateStatusInfo.setBindId(jsonObject.getStr("bind_id"));
            }
        }
        return privateStatusInfo;
    }

    /**
     * @param privateStatusInfo 推送服务参数
     * @return String
     */
    @Override
    @ApiOperation("调用private-num-push服务实现")
    public String putPrivateNumPush(PrivateStatusInfo privateStatusInfo) {
        log.info("通话状态转换后的报文：{}", JSON.toJSONString(privateStatusInfo));
        log.info("开始通话状态feign调用");
        Result result;
        try {
            result = numberPushFeignService.statusReceiver(privateStatusInfo);
            log.info("通话状态返回结果：{}", result);
        } catch (Exception e) {
            log.error("通话状态feign调用失败", e);
            rabbitMqSender.send(privateStatusInfo, UnicomRabbitMqConfig.NUM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.NUM_PUSH_DELAYED_ROUTING, 0);
            return UnicomCommonEnum.FAIL.getValue();
        }
        log.info("通话状态feign返回：{}", result.toString());
        if ("成功".equals(result.getMessage())) {
            return UnicomCommonEnum.SCUESS.getValue();
        }
       // rabbitMqSender.send(privateStatusInfo, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_ROUTING, 0);
        return UnicomCommonEnum.FAIL.getValue();
    }


}
