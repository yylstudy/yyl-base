package com.cqt.hmyc.web.bind.service.hdh;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.cloud.api.sms.SmsPushBillFeignClient;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.SmsResultCodeEnum;
import com.cqt.common.util.AuthUtil;
import com.cqt.common.util.ThirdUtils;
import com.cqt.hmyc.config.properties.HdhProperties;
import com.cqt.hmyc.config.rabbitmq.DelayedPushRabbitConfig;
import com.cqt.hmyc.web.bind.manager.MqSender;
import com.cqt.hmyc.web.bind.service.LocalOrLongService;
import com.cqt.hmyc.web.corpinfo.service.IPrivateCorpBusinessInfoService;
import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.hmyc.web.model.hdh.push.sms.IccpSmsStatePush;
import com.cqt.hmyc.web.model.hdh.push.sms.SmsRequest;
import com.cqt.hmyc.web.model.hdh.push.sms.SmsRequestBody;
import com.cqt.hmyc.web.model.hdh.push.sms.SmsRequestHeader;
import com.cqt.model.common.ThirdPushResult;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

/**
 *  （hdh）接收短信话单推送 处理
 *
 * @author dingsh
 * date 2022/08/03
 */
@Service
@Slf4j
public class HdhSmsPushService extends HdhBaseService {

    private final MqSender mqSender;

    private final SmsPushBillFeignClient smsPushBillFeignClient;

    private final RedissonUtil redissonUtil;

    private final LocalOrLongService localOrLongService;

    private final IPrivateCorpBusinessInfoService corpBusinessService;

    private final ThreadPoolTaskExecutor saveExecutor;

    public HdhSmsPushService(HdhProperties hdhProperties, RedissonUtil redissonUtil, MqSender mqSender,
                             SmsPushBillFeignClient smsPushBillFeignClient,
                             RedissonUtil redissonUtil1, LocalOrLongService localOrLongService,
                             IPrivateCorpBusinessInfoService corpBusinessService,
                             ThreadPoolTaskExecutor saveExecutor) {
        super(hdhProperties, redissonUtil);
        this.mqSender = mqSender;
        this.smsPushBillFeignClient = smsPushBillFeignClient;
        this.redissonUtil = redissonUtil1;
        this.localOrLongService = localOrLongService;
        this.corpBusinessService = corpBusinessService;
        this.saveExecutor = saveExecutor;
    }

    /**
     *  (hdh)接收话单推送 处理
     */
    public ThirdPushResult hdhSmsPush(HdhPushIccpDTO hdhPushIccpDTO){
        log.info("接收到短信话单  hdhPushIccpDTO： {}", JSONUtil.toJsonStr(hdhPushIccpDTO));
        //判断是否无绑定关系
        if(SDR_UNBIND_CODE.equals(hdhPushIccpDTO.getSmsResult())){
            log.info("当前短信话单为无绑定话单 不推送：body: {} ",JSONUtil.toJsonStr(hdhPushIccpDTO));
            return ThirdPushResult.ok(ThirdConstant.HDH_SUCCESS_CODE,"成功");
        }
        //根据bindId 获取本平台 信息缓存
        hdhPushIccpDTO=addCqtInfo(hdhPushIccpDTO);

        //转换话单实体 提交话单入库mq
        SmsRequest smsRequest = buildSmsRequest(hdhPushIccpDTO);
        mqSender.send(smsRequest, ThirdConstant.ICCPSMSCDRSAVEEXCHANGE,ThirdConstant.ICCPSMSCDRSAVEROUTEKEY,0);
        //调用push 服务
        IccpSmsStatePush iccpSmsStatePush=buildIccpSms(hdhPushIccpDTO);
        String msgRequest= JSONObject.toJSONString(iccpSmsStatePush);
        saveExecutor.execute(() -> {
                CdrResult cdrResult;
                try {
                    cdrResult = smsPushBillFeignClient.smsThirdPush(msgRequest);
                    if (ThirdConstant.HDH_SUCCESS_CODE.equals(cdrResult.getResult())) {
                        log.info("调用 push 成功，body: {}", msgRequest);
                    }else {
                        mqSender.send(iccpSmsStatePush, DelayedPushRabbitConfig.CDR_SMS_PUSH_DELAYED_EXCHANGE, DelayedPushRabbitConfig.CDR_SMS_PUSH_DELAYED_ROUTING, 0);
                    }
                } catch (Exception e) {
                    mqSender.send(iccpSmsStatePush, DelayedPushRabbitConfig.CDR_SMS_PUSH_DELAYED_EXCHANGE, DelayedPushRabbitConfig.CDR_SMS_PUSH_DELAYED_ROUTING, 0);
                    log.info("调用push 服务异常 请求消息：{} ，e: {}", msgRequest, e);
                }
        });
        log.info("##########################短信 话单返回成功");
        return  ThirdPushResult.ok(ThirdConstant.HDH_SUCCESS_CODE,"成功");
    }


    /**
     * 构造话单实体
     */
    private SmsRequest buildSmsRequest(HdhPushIccpDTO hdhPushIccpDTO) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //获取 失败码
        //HdhSmsResultCodeEnum smsResultCodeEnum=ThirdUtils.hdhSmsResultCode(hdhPushIccpDTO.getSmsResult());
        SmsResultCodeEnum smsResultCodeEnum = ThirdUtils.callSmsResultCode(hdhPushIccpDTO.getSmsResult());
        SmsRequest sendSmsRequest = new SmsRequest();
        SmsRequestBody body = new SmsRequestBody();
        SmsRequestHeader header = new SmsRequestHeader();
        header.setStreamNumber(uuid);
        header.setMessageId(hdhPushIccpDTO.getCallId());
        body.setBindId(hdhPushIccpDTO.getBindId());
        body.setAPhoneNumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()));
        body.setBPhoneNumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()));
        body.setInPhoneNumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()));
        body.setOutPhoneNumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()));
        body.setVccId(hdhPushIccpDTO.getVccId());
        body.setRequestTime(ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getSmsTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss));
        body.setSendTime(ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getSmsTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss));
        body.setFailCode(String.valueOf(smsResultCodeEnum.getCode()));
        body.setFailReason(smsResultCodeEnum.getDesc());
        body.setCallerNumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()));
        body.setCalledNumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()));
        body.setSmsNumber(hdhPushIccpDTO.getSmsNumber());
        body.setSupplierId(hdhPushIccpDTO.getSupplierId());
        // TODO 目前第三方没有内容字段，先传空，字段落实后补充
        body.setInContent(StringUtils.isBlank(hdhPushIccpDTO.getSmsContent())? "":hdhPushIccpDTO.getSmsContent());
        sendSmsRequest.setHeader(header);
        sendSmsRequest.setBody(body);

        return sendSmsRequest;

    }

    /**
     *  构造话单实体
     */
    private IccpSmsStatePush buildIccpSms(HdhPushIccpDTO hdhPushIccpDTO){
        String vccId=hdhPushIccpDTO.getVccId();
        //获取 失败码
        SmsResultCodeEnum smsResultCodeEnum=ThirdUtils.callSmsResultCode(hdhPushIccpDTO.getSmsResult());
        //获取x号码归属地
        String xAreaCode= localOrLongService.checkMobilePhone(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()));

        IccpSmsStatePush iccpSmsStatePush= IccpSmsStatePush.builder()
                .ts(String.valueOf(System.currentTimeMillis()))
                .area_code(xAreaCode)
                .appId(hdhPushIccpDTO.getVccId())
                .appkey(hdhPushIccpDTO.getVccId())
                .sms_id(StringUtils.isBlank(hdhPushIccpDTO.getCallId())? "":hdhPushIccpDTO.getCallId())
                .bind_id(hdhPushIccpDTO.getBindId())
                .sender(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()))
                .receiver(StringUtils.isBlank(hdhPushIccpDTO.getPeerNo())? "":ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()))
                .sender_show(StringUtils.isBlank(hdhPushIccpDTO.getX())? "":ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()))
                .receiver_show(StringUtils.isBlank(hdhPushIccpDTO.getX())? "":ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()))
                .transfer_time(StringUtils.isBlank(hdhPushIccpDTO.getSmsTime())? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getSmsTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .user_data(hdhPushIccpDTO.getUserData())
                .sms_number(hdhPushIccpDTO.getSmsNumber())
                .sms_result(String.valueOf(smsResultCodeEnum.getCode()))
                // TODO 目前第三方没有内容字段，先传空，字段落实后补充
                .sms_content(StringUtils.isBlank(hdhPushIccpDTO.getSmsContent())? "":hdhPushIccpDTO.getSmsContent())
                .build();
        TreeMap paramsMap = JSON.parseObject(JSON.toJSONString(iccpSmsStatePush), TreeMap.class);
        String secretKey=getSecreKey(vccId);
        String sign = AuthUtil.createSign(paramsMap, vccId, secretKey);
        iccpSmsStatePush.setSign(sign);
        return  iccpSmsStatePush;
    }

    /**
     *  获取企业的secreKey
     */
    private String getSecreKey(String vccId){
        //业务配置key
        PrivateCorpBusinessInfo businessInfo;
        String busKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccId);
        String o1 = redissonUtil.getString(busKey);
        if (StringUtils.isNotBlank(o1)) {
            businessInfo=JSONUtil.toBean(o1,PrivateCorpBusinessInfo.class);
        } else {
            QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("vcc_id", vccId);
            businessInfo = corpBusinessService.getOne(wrapper);
        }
        String secretKey=businessInfo.getSecretKey();
        log.info("当前企业sereKey: {}",secretKey);
        return  secretKey;
    }

}
