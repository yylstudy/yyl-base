package com.cqt.monitor.web.callevent.rabbitmq;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.cqt.model.monitor.constants.RedisConstant;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.monitor.web.callevent.entity.Callstat;
import com.cqt.monitor.web.callevent.entity.EventInMin;
import com.cqt.monitor.web.callevent.entity.PlatProperty;
import com.cqt.monitor.web.callevent.mapper.EventInMinMapper;
import com.cqt.monitor.web.callevent.mapper.PrivateNumberInfoMapper;
import com.cqt.redis.util.RedissonUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.cqt.monitor.web.callevent.jobhandler.WarningJob.getTimeByPattern;

@Slf4j
@Component
public class MqConsumer {

    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private PlatProperty platProperty;
    @Autowired
    private EventInMinMapper eventInMinMapper;

    @Autowired
    private PrivateNumberInfoMapper privateNumberInfoMapper;


    private static final String TABLE_PRE = "private_call_event_stats_";

    /**
     * 消费话单队列, 消费者最大数量20
     */
    @RabbitListener(queues = "private_call_event_stats_queues")
    public void messageHandler(Message message, Channel channel) {
        log.info("收到private_call_event_stats_queues队列消息");

        try {
            //业务处理
            String keyInCorp;
            String keyInNum;
            String formValue = platProperty.getFormValue();
            if ("nj".equals(platProperty.getFormValue())) {
                keyInCorp = RedisConstant.GRANULARITY_IN_CORPNJ;
                keyInNum = RedisConstant.GRANULARITY_IN_NUMNJ;
            } else {
                keyInCorp = RedisConstant.GRANULARITY_IN_CORPYZ;
                keyInNum = RedisConstant.GRANULARITY_IN_NUMYZ;
            }
            Callstat callstat = JSONObject.parseObject(new String(message.getBody()), Callstat.class);
            if ("900007".equals(callstat.getServicekey()) && StringUtils.isNotEmpty(callstat.getGroupnumber())) {
                String vccId = callstat.getGroupnumber();
                String areaCode = callstat.getKey3();
                String xNum = callstat.getSpecificchargedpar();
                String format = String.format(RedisConstant.PRIVATE_NUMBER_INFO, xNum);
                String string = redissonUtil.getString(format);
                if (StringUtils.isEmpty(string)) {
                    PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(xNum);
                    if (ObjectUtil.isEmpty(privateNumberInfo)) {
                        log.info("{} 号码查询缓存为空,rediskey:{}", xNum, format);
                        return;
                    }
                    redissonUtil.setString(format,JSONObject.toJSONString(privateNumberInfo));
                    string = JSONObject.toJSONString(privateNumberInfo);
                }
                PrivateNumberInfo privateNumberInfo = JSONObject.parseObject(string, PrivateNumberInfo.class);

                String supplierId = privateNumberInfo.getSupplierId();
                String current = getTimeByPattern(System.currentTimeMillis(), "yyyyMMddHHmm");
                String formatCorp = String.format(keyInCorp, vccId, areaCode, current, supplierId);
                String formatNum = String.format(keyInNum, vccId, areaCode, xNum, current, supplierId);
                log.info("企业颗粒度redis key：{},企业号码颗粒度redis key：{}", formatCorp, formatNum);
                redissonUtil.addHashItem(formValue, formatCorp, "cdr", 1L, 14400L, TimeUnit.SECONDS);
                redissonUtil.addHashItem(formValue, formatNum, "cdr", 1L, 14400L, TimeUnit.SECONDS);

                if (StringUtils.isNotEmpty(callstat.getExtforwardnumber())) {
                    redissonUtil.addHashItem(formValue, formatCorp, "ring", 1L, 14400L, TimeUnit.SECONDS);
                    redissonUtil.addHashItem(formValue, formatNum, "ring", 1L, 14400L, TimeUnit.SECONDS);
                }
                if (StringUtils.isNotEmpty(callstat.getDuration()) && Integer.parseInt(callstat.getDuration()) > 0) {
                    redissonUtil.addHashItem(formValue, formatCorp, "pick", 1L, 14400L, TimeUnit.SECONDS);
                    redissonUtil.addHashItem(formValue, formatNum, "pick", 1L, 14400L, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            log.error("(warn)话单队列消费失败=>", e);
        } finally {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("ack异常=>", e);
            }
            log.info("消费完成");
        }

    }

    @RabbitListener(queues = "private_warn_event_queues")
    public void eventConsumer(Message message, Channel channel) {
        try {
            EventInMin event = JSONObject.parseObject(new String(message.getBody()), EventInMin.class);
            String currentMin = event.getCurrentMin();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String tableEnd1 = df.format(df.parse(currentMin));
            String tableEnd = tableEnd1.replace("-", "");
            String tableName = TABLE_PRE + tableEnd;
            String id = UUID.randomUUID().toString().replaceAll("-", "");
            log.info("每分鐘話單數據入庫");
            eventInMinMapper.insertByCondition(tableName, event, id);
        } catch (Exception e) {
            log.error("消费private_warn_event_queues队列入库失败：" + e);
        } finally {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("ack异常=>", e);
            }
        }
    }






}
