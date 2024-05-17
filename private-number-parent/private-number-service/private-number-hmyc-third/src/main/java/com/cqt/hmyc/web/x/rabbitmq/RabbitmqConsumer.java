package com.cqt.hmyc.web.x.rabbitmq;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.hdh.HdhPushService;
import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.hmyc.web.model.hdh.push.HdhXPushIccpDTO;
import com.cqt.redis.util.RedissonUtil;
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

    private final DelayProducer delayProducer;

    private final HdhPushService pushService;

    @RabbitListener(queues = AxbBillDelayConfig.DEAD_LETTER_QUEUEB_NAME)
    public void deadLetterMsg(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        String s = new String(message.getBody());
        log.info("延时队列收到话单：{}",s);
        HdhXPushIccpDTO hdhPushIccpDTO = JSON.parseObject(s, HdhXPushIccpDTO.class);
        String recordUrlKey = PrivateCacheUtil.getThirdRecordUrlKey(hdhPushIccpDTO.getCallId());
        String recordUrl = redissonUtil.getString(recordUrlKey);
        try {
            if (StringUtil.isNotEmpty(recordUrl)){
                hdhPushIccpDTO.setRecordUrl(recordUrl);
                pushService.hdhXPush(hdhPushIccpDTO);
                redissonUtil.delKey(recordUrlKey);
                return;
            }
            String xout = redissonUtil.getString(hdhPushIccpDTO.getCallId());
            if (StringUtil.isEmpty(xout)||Integer.parseInt(xout)<5){
                delayProducer.send(hdhPushIccpDTO);
                redissonUtil.increment(hdhPushIccpDTO.getCallId(), Duration.ofMinutes(10));
                log.info("count=>{}|话单关联录音失败，重推回mq callid => {}",redissonUtil.getString(hdhPushIccpDTO.getCallId()),hdhPushIccpDTO.getCallId());
            }else {
                log.info("count=>{}|重推超过次数，不再重推|（话单关联录音字段延迟队列）callid => {}",xout,hdhPushIccpDTO.getCallId());
                redissonUtil.delKey(hdhPushIccpDTO.getCallId());
                pushService.hdhXPush(hdhPushIccpDTO);
            }
        }catch (Exception e){
            log.error("消费异常："+e);
        }

    }


}
