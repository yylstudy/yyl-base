package com.cqt.broadnet.config.rabbitmq;

import com.alibaba.fastjson.JSONException;
import com.cqt.common.enums.AckActionEnum;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.redisson.client.RedisException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLSyntaxErrorException;

/**
 * @author linshiqiang
 * @date 2021/10/26 9:39
 */
@Slf4j
public class RabbitmqAck {

    public static AckActionEnum getAckActionEnum(Exception e) {
        AckActionEnum action;
        action = AckActionEnum.RETRY;
        if (e instanceof RedisException) {
            return AckActionEnum.REJECT;
        }
        if (e instanceof JSONException) {
            return AckActionEnum.REJECT;
        }
        if (e instanceof DuplicateKeyException) {
            return AckActionEnum.REJECT;
        }
        if (e instanceof NullPointerException) {
            return AckActionEnum.REJECT;
        }
        if (e instanceof SQLSyntaxErrorException) {
            return AckActionEnum.REJECT;
        }
        if (e instanceof BadSqlGrammarException) {
            return AckActionEnum.REJECT;
        }
        if (e instanceof MyBatisSystemException) {
            return AckActionEnum.REJECT;
        }
        return action;
    }

    public static void ackDeal(Channel channel, long deliveryTag, AckActionEnum action) {
        try {
            switch (action) {
                case ACCEPT:
                    channel.basicAck(deliveryTag, true);
                    break;
                case RETRY:
                    channel.basicNack(deliveryTag, false, true);
                    break;
                case REJECT:
                    channel.basicNack(deliveryTag, false, false);
                    break;
                default:
                    channel.basicAck(deliveryTag, true);
                    break;
            }
        } catch (Exception e) {
            log.error("ack error: {}", e.getMessage());
        }
    }
}
