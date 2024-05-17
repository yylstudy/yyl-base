package com.cqt.unicom.config.rabbitmq;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.model.common.Result;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.unicom.entity.*;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.unicom.feign.NumberPushFeignService;
import com.cqt.unicom.feign.SmsPushFeignService;
import com.cqt.unicom.mapper.PrivateCorpInteriorInfoMapper;
import com.cqt.unicom.properties.VccIdCheckProperties;
import com.cqt.unicom.service.UnicomCallListPushServiceImpl;
import com.cqt.unicom.util.UnicomUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;


/**
 * 消费重送数据
 *
 * @author zhengsuhao
 * @date 2022/12/14
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqReceiver {


    private final RestTemplate restTemplate;


    private final RedissonUtil redissonUtil;


    private final NumberPushFeignService numberPushFeignService;


    private final SmsPushFeignService smsPushFeignService;


    private final PrivateCorpInteriorInfoMapper privateCorpInteriorInfoMapper;


    private final UnicomCallListPushServiceImpl pushService;

    private final VccIdCheckProperties checkProperties;

    private final RabbitMqSender rabbitMqSender;

    @RabbitListener(queues = UnicomRabbitMqConfig.COMM_PUSH_DEAD_LETTER_QUEUE)
    @RabbitHandler
    public void deadCommLetterMsg(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("开始重送：{}", msg);
        CustomerReceivesDataInfo customerReceivesDataInfo = JSON.parseObject(msg, CustomerReceivesDataInfo.class);
        String vccid = customerReceivesDataInfo.getVccId();
        String customerUrl = null;
        try {
            String corpInfo = redissonUtil.getString(String.format(PrivateCacheConstant.CORP_INTERIOR_INFO, vccid, 900007));
            //判读缓存获取是否非空
            if (!StringUtil.isBlank(corpInfo)) {
                PrivateCorpInteriorInfo privateCorpInteriorInfo = JSON.parseObject(corpInfo, PrivateCorpInteriorInfo.class);
                //判读字段是否非空
                if (ObjectUtil.isNotEmpty(privateCorpInteriorInfo)&&StringUtil.isBlank(privateCorpInteriorInfo.getVoiceCdrUrl())) {
                    customerUrl = privateCorpInteriorInfo.getVoiceCdrUrl();
                }
            }            //redis找不到查数据库
            if (StrUtil.isBlank(customerUrl)) {
                customerUrl = privateCorpInteriorInfoMapper.selectByVccId(vccid, "900007");
            }
            log.info("客户对应url：{}", customerUrl);
            CommResult commResult;
            if (!checkProperties.getCommon().contains(customerReceivesDataInfo.getVccId())){
                customerReceivesDataInfo.setCallAcrUrl (checkProperties.getThirdUrl ());
                commResult = pushService.sendPrivatePush(customerReceivesDataInfo);
            }else {
                commResult =  restTemplate.postForObject(customerUrl, customerReceivesDataInfo, CommResult.class);

            }
            log.info("客户对应vccid：{}|返回结果：{}",vccid,commResult);
            if (commResult == null || !UnicomCommonEnum.SCUESS.getValue().contains(commResult.getReason())) {
                log.info("推送失败，重新推送:"+commResult);
                rabbitMqSender.send(customerReceivesDataInfo, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_ROUTING, 0);
            }
        } catch (Exception e) {
            log.error("vccid: {},消息内容：{},操作异常: ", vccid,msg,e);
            rabbitMqSender.send(customerReceivesDataInfo, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_ROUTING, 0);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }

    @RabbitListener(queues = UnicomRabbitMqConfig.NUM_PUSH_DEAD_LETTER_QUEUE)
    @RabbitHandler
    public void deadNumLetterMsg(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("接收到通话状态重推消息：{}", msg);
        Map<String, Object> map = JSONObject.parseObject(msg, Map.class);
        PrivateStatusInfo privateStatusInfo = null;
        try {
            privateStatusInfo = UnicomUtil.mapToObject(map, PrivateStatusInfo.class);
        } catch (InstantiationException e) {
            log.error("对象转换异常：", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Result result;
        try {
            result = numberPushFeignService.statusReceiver(privateStatusInfo);
            if (result == null || !"成功".equals(result.getMessage())) {
                rabbitMqSender.send(privateStatusInfo, UnicomRabbitMqConfig.NUM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.NUM_PUSH_DELAYED_ROUTING, 0);
            }
        } catch (Exception e) {
            log.error("操作异常: ", e);
            rabbitMqSender.send(privateStatusInfo, UnicomRabbitMqConfig.NUM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.NUM_PUSH_DELAYED_ROUTING, 0);
        }
        log.info("收到通话状态重推 时间:" + DateUtil.now() + "--消息: {}", msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    @RabbitListener(queues = UnicomRabbitMqConfig.SMS_PUSH_DEAD_LETTER_QUEUE)
    @RabbitHandler
    public void deadSmsLetterMsg(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("接收到短信话单重推消息：{}", msg);
        MeituanSmsStatePush meituanSmsStatePush = JSON.parseObject(msg, MeituanSmsStatePush.class);
        CdrResult result;
        try {
            if(StrUtil.isNotBlank (meituanSmsStatePush.getAppkey ())){
                return;
            }
            result = smsPushFeignService.smsPush(meituanSmsStatePush);
            if (result == null || !"0000".equals(result.getResult())) {
                rabbitMqSender.send(meituanSmsStatePush, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_ROUTING, 0);
            }
        } catch (Exception e) {
            log.error("操作异常: ", e);
            rabbitMqSender.send(meituanSmsStatePush, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.SMS_PUSH_DELAYED_ROUTING, 0);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        log.info("收到短信话单重推 时间:" + DateUtil.now() + "--消息: {}", msg);

    }

}



