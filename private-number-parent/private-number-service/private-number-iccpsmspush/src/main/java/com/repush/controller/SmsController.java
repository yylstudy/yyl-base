/**
 * Copyright ? 2017 公司名. All rights reserved.
 */
package com.repush.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.repush.dao.domain.SmsSdr;
import com.repush.dao.domain.SmsStatePush;
import com.repush.model.entity.SmsRequest;
import com.repush.model.entity.SmsRequestBody;
import com.repush.model.entity.SmsRequestHeader;
import com.repush.mqRend.MqSender;
import com.repush.service.SmsService;
import com.repush.util.EmojiUtils;
import com.repush.util.JsonUtil;
import com.repush.util.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author youngder
 */
@Controller
@Log4j2
public class SmsController {

    //短信账单记录表
    private static final String TABLE_NAME = "sms_sdr_";
    //号码所属运营商本地缓存
    private static Map<String, String> operatorMap = new HashMap<String, String>();
    @Resource
    private SmsService smsService;
    @Autowired
    private MqSender mqSender;


    /**
     * 消费短信账单
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "iccp_sms_sdr_queues")
    public void consumeMessage(Channel channel, Message message) throws IOException {
        String uuid = StringUtil.getUUID();
        String msgRequest = "";
        try {
            msgRequest = new String(message.getBody());
            log.info(uuid + "|从mq获取到短信账单" + msgRequest);
            JSONObject jsonObject = JSONObject.parseObject(msgRequest);
            SmsRequest smsRequest = jsonObject.toJavaObject(SmsRequest.class);
            SmsRequestHeader header = smsRequest.getHeader();
            SmsRequestBody body = smsRequest.getBody();
            SmsSdr smsSdr = new SmsSdr();
            smsSdr.setAreaCode(body.getAreaCode());
            //流水号
            smsSdr.setStreamNumber(header.getStreamNumber());
            //messageId
            smsSdr.setMessageId(header.getMessageId());
            //发短信号码a
            smsSdr.setCallerNumber(body.getAPhoneNumber());
            //入中间号
            smsSdr.setInNumber(body.getInPhoneNumber());
            //出中间号
            smsSdr.setOutNumber(body.getOutPhoneNumber());
            //收短信号码
            smsSdr.setCalledNumber(body.getBPhoneNumber());
            //短信内容
            smsSdr.setInContent(EmojiUtils.removeFourChar (  body.getInContent()));
            //请求时间
            smsSdr.setRequestTime(body.getRequestTime());
            //实际发短信时间（请求map时间）
            smsSdr.setSendTime(body.getSendTime());
            //短信发送状态（错误码）
            smsSdr.setFailCode(body.getFailCode());
            //失败原因
            smsSdr.setFailReason(body.getFailReason());
            smsSdr.setBindId(body.getBindId());
            smsSdr.setVccId(body.getVccId());
            smsSdr.setBindId(body.getBindId());
            smsSdr.setSmsNumber(body.getSmsNumber());
            smsSdr.setSupplierId(body.getSupplierId());
            String tableName = TABLE_NAME + Convert.toStr(body.getVccId(), "0000") + "_" + StringUtil.getDate6();
            if (StrUtil.isBlank(body.getVccId())) {
                tableName = TABLE_NAME + "0000_" + StringUtil.getDate6();
                smsSdr.setRequestTime(DateUtil.formatDateTime(DateUtil.date()));
                smsSdr.setSendTime(DateUtil.formatDateTime(DateUtil.date()));
            }
            smsSdr.setTableName(tableName);
            //保存短信记录
            try {
                smsService.saveSmsSdr(smsSdr);
                log.info(uuid + "|短信记录入库成功");
            } catch (Exception e) {
                log.error("sms cdr insert error： ", e);
                try {
                    smsService.createSmsTable(tableName);
                } catch (Exception ex) {
                    log.error("createSmsTable: ", ex);
                }
                smsService.saveSmsSdr(smsSdr);
                log.error(uuid + "|短信记录入库成功");
            }
        } catch (Exception e) {
            log.error(uuid + "|未知错误，短信记录入库失败");
            log.error("iccp_sms_sdr_queues consumer error: ", e);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 消费短信账单
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "private_num_sms_queue")
    public void smsStateRepush(Channel channel, Message message) {
        String uuid = StringUtil.getUUID();
        String msgRequest = "";
        msgRequest = new String(message.getBody());
        log.info(uuid + "从mq获取到状态推送" + msgRequest);
        SmsStatePush smsStatePush = JsonUtil.jsonToPojo(msgRequest, SmsStatePush.class);
        String potsResult = "";
        try {
            if ((Integer.parseInt(smsStatePush.getNum()) > 2)) {
                log.info(uuid + "|超过最大重推次数,写入数据库不在重推");
                smsStatePush.setErrMsg("超过最大重推次数");
                smsStatePush.setId(uuid);
                smsService.saveSmsFailedStatePush(smsStatePush);
            } else {
                potsResult = HttpUtil.post(smsStatePush.getUrl(), smsStatePush.getJson());
                log.info(uuid + "|状态推送客户结果：" + potsResult);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            try {
                //推送失败，写入mq
                log.info(uuid + "|状态推送失败，写入mq进行重推");
                smsStatePush.setErrMsg(potsResult);
                smsStatePush.setNum(String.valueOf(Integer.parseInt(smsStatePush.getNum()) + 1));
                mqSender.send(JsonUtil.objectToJson(smsStatePush), 3000);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
