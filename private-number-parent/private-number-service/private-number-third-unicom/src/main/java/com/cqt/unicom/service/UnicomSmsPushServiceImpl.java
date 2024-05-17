package com.cqt.unicom.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.unicom.dto.SmsStatusDTO;
import com.cqt.model.unicom.entity.*;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.unicom.config.rabbitmq.RabbitMqSender;
import com.cqt.unicom.config.rabbitmq.UnicomRabbitMqConfig;
import com.cqt.unicom.feign.SmsPushFeignService;
import com.cqt.unicom.mapper.PrivateNumberInfoMapper;
import com.cqt.unicom.util.UnicomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhengsuhao
 * @date 2022/12/12
 */
@Api(tags = "联通集团总部(江苏)能力:短信状态服务")
@Slf4j
@Service
@RequiredArgsConstructor
public class UnicomSmsPushServiceImpl implements UnicomSmsPushService {


    private final RedissonUtil redissonUtil;


    private final RabbitMqSender rabbitMqSender;


    private final PrivateNumberInfoMapper numberInfoMapper;


    private final SmsPushFeignService smsPushFeignService;


    /**
     * @param smsStatusDTO 联通集团总部(江苏)短信状态推送入参
     * @return SmsRequest
     */
    @Override
    @ApiOperation("集团报文转换客户报文服务实现")
    public SmsRequest getSmsStatus(SmsStatusDTO smsStatusDTO) {
        log.info("江苏联通送短信话单原始入参：{}", smsStatusDTO);
        //插入流水号
        String streamNumber = String.format("%s-%s", smsStatusDTO.getCallId(), smsStatusDTO.getBindingId());
        //插入业务流水号
        String messageId = String.format("%s-%s", smsStatusDTO.getBindingId(), smsStatusDTO.getCallId());
        //插入短信标识
        String messageReference = smsStatusDTO.getCallId();
        SmsRequestHeader smsRequestHeader = new SmsRequestHeader(streamNumber, messageId, messageReference);
        SmsRequestBody smsRequestBody = new SmsRequestBody();
        //插入主叫号码
        smsRequestBody.setAPhoneNumber(smsStatusDTO.getPhoneNumberA());
        //插入被叫号码
        smsRequestBody.setBPhoneNumber(smsStatusDTO.getPhoneNumberB());
        //插入绑定ID
        smsRequestBody.setBindId(smsStatusDTO.getBindingId());
        //插入入服务号
        smsRequestBody.setInPhoneNumber(smsStatusDTO.getPhoneNumberX());
        //插入出服务号
        smsRequestBody.setOutPhoneNumber(smsStatusDTO.getPhoneNumberX());
        smsRequestBody.setRequestTime(smsStatusDTO.getReceiveTime());
        //插入发送事件
        smsRequestBody.setSendTime(smsStatusDTO.getReceiveTime());
        //插入主叫号码（发短信号码）
        smsRequestBody.setCallerNumber(smsStatusDTO.getPhoneNumberA());
        //插入被叫号码（收短信号码）
        smsRequestBody.setCalledNumber(smsStatusDTO.getPhoneNumberB());
        //插入失败码
        if (!String.valueOf(UnicomCommonEnum.ZERO.getValue()).equals(smsStatusDTO.getState())) {
            SmsCodeEnum smsCodeEnum = UnicomUtil.smsResultCodxe(smsStatusDTO.getState());
            smsRequestBody.setFailCode(String.valueOf(smsCodeEnum.getCode()));
            smsRequestBody.setFailReason(smsCodeEnum.getDesc());
        }
        //插入vccid
        String vccid = null;
        try {
            vccid = redissonUtil.getString(String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, smsStatusDTO.getPhoneNumberX()));
            //如果找不到查数据库
            if (StringUtil.isEmpty(vccid)) {
                PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(smsStatusDTO.getPhoneNumberX());
                vccid = privateNumberInfo.getVccId();
            }
        } catch (Exception e) {
            log.error("redisson 操作异常: ", e);
        }
        smsRequestBody.setVccId(vccid);
        smsRequestBody.setBindId(smsStatusDTO.getBindingId());
        if (StringUtil.isNotEmpty(smsStatusDTO.getSmsContent())) {
            smsRequestBody.setInContent(HexUtil.decodeHexStr(smsStatusDTO.getSmsContent()));
        }
        return new SmsRequest(smsRequestHeader, smsRequestBody);
    }

    /**
     * @param smsRequest 短信请求参数
     * @return String
     */
    @Override
    @ApiOperation("报文放入消息队列服务实现")
    public String setMessageQueue(SmsRequest smsRequest) {
        log.info("接收到短信话单SmsRequest： {}", JSONUtil.toJsonStr(smsRequest));
        try {
            rabbitMqSender.send(smsRequest, ThirdConstant.ICCPSMSCDRSAVEEXCHANGE, ThirdConstant.ICCPSMSCDRSAVEROUTEKEY, 0);
        } catch (Exception e) {
            log.error("短信话单队列调用失败", e);
            return UnicomCommonEnum.FAIL.getValue();
        }
        return UnicomCommonEnum.SCUESS.getValue();
    }

    /**
     * @param meituanSmsStatePush 短信请求参数
     * @return String
     */
    @Override
    public String putPrivateNumSms(MeituanSmsStatePush meituanSmsStatePush) {
        log.info("短信话单feign调用");
        CdrResult result;
        try {
            result = smsPushFeignService.smsPush(meituanSmsStatePush);
        } catch (Exception e) {
            log.error("短信话单feign调用异常", e);
            rabbitMqSender.send(meituanSmsStatePush, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_ROUTING, 0);
            return UnicomCommonEnum.FAIL.getValue();
        }
        log.info("短信话单feign返回：{}", result.toString());
        if ("0000".equals(result.getResult())) {
            return UnicomCommonEnum.SCUESS.getValue();
        }
        rabbitMqSender.send(meituanSmsStatePush, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_ROUTING, 0);
        return UnicomCommonEnum.FAIL.getValue();

    }

    @Override
    public MeituanSmsStatePush getMeituanSmsStatePush(SmsRequest smsRequest) {
        MeituanSmsStatePush meituanSmsStatePush = new MeituanSmsStatePush();
        SmsRequestBody body = smsRequest.getBody();
        meituanSmsStatePush.setAppkey(body.getVccId());
        meituanSmsStatePush.setTs(System.currentTimeMillis());
        meituanSmsStatePush.setSmsId(smsRequest.getHeader().getStreamNumber());
        meituanSmsStatePush.setBindId(body.getBindId());
        meituanSmsStatePush.setSender(body.getAPhoneNumber());
        meituanSmsStatePush.setSenderShow(body.getInPhoneNumber());
        meituanSmsStatePush.setReceiverShow(body.getBPhoneNumber());
        meituanSmsStatePush.setTransferTime(DateUtil.now());
        //需要插入area_code字段，根据中间号前7位判断获取
        String telCode = body.getInPhoneNumber().substring(0, 7);
        meituanSmsStatePush.setAreaCode(redissonUtil.getString("h_" + telCode));
        //需要插入sms_content字段，暂时没有
        meituanSmsStatePush.setSmsContent(smsRequest.getBody().getInContent());
        meituanSmsStatePush.setSmsResult(0);
        return meituanSmsStatePush;
    }
}
