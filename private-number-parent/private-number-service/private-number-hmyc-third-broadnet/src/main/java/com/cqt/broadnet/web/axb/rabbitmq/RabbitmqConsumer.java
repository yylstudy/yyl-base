package com.cqt.broadnet.web.axb.rabbitmq;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.broadnet.web.axb.service.AxbCallBillStorePushService;
import com.cqt.broadnet.web.axb.service.AxbCallNotificationService;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * @author huweizhong
 * date  2023/6/8 9:17
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitmqConsumer {

    private final RedissonUtil redissonUtil;

    private final AxbCallBillStorePushService axbCallBillStorePushService;

    private final AxbCallNotificationService axbCallNotificationService;

    private final ObjectMapper objectMapper;

    private final DelayProducer delayProducer;

    @RabbitListener(queues = AxbBillDelayConfig.DEAD_LETTER_QUEUEB_NAME)
    public void deadLetterMsg(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        String s = new String(message.getBody());
        PrivateBillInfo privateBillInfo = JSON.parseObject(s, PrivateBillInfo.class);
        String recordUrlKey = PrivateCacheUtil.getRecordUrlKey(privateBillInfo.getRecordId());
        String recordUrl = redissonUtil.getString(recordUrlKey);
        redissonUtil.increment(privateBillInfo.getRecordId(), Duration.ofMinutes(10));
        try {
            if (StringUtil.isNotEmpty(recordUrl)){
                privateBillInfo.setRecordFileUrl(recordUrl);
                Callstat callstat = axbCallNotificationService.toCallstat(privateBillInfo);
                axbCallBillStorePushService.storeCallBill(privateBillInfo, callstat);
                redissonUtil.delKey(recordUrlKey);
                return;
            }
            String xout = redissonUtil.getString(privateBillInfo.getRecordId());
            if (Integer.parseInt(xout)<3){
                delayProducer.send(privateBillInfo);
                log.info("count=>{}|话单关联录音失败，重推回mq callid => {}",xout,privateBillInfo.getRecordId());
            }else {
                log.info("count=>{}|重推超过次数，不再重推|（话单关联录音字段延迟队列）callid => {}",xout,privateBillInfo.getRecordId());
                redissonUtil.delKey(privateBillInfo.getRecordId());
                Callstat callstat = axbCallNotificationService.toCallstat(privateBillInfo);
                axbCallBillStorePushService.storeCallBill(privateBillInfo, callstat);
            }
        }catch (Exception e){
            log.error("消费异常："+e);
        }

    }


}
