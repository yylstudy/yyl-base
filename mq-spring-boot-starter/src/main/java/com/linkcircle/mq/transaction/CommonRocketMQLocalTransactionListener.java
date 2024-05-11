package com.linkcircle.mq.transaction;

import com.linkcircle.mq.config.MqApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 事务消息监听器
 * @createTime 2022/1/26 14:10
 */

@ConditionalOnClass(name="org.apache.rocketmq.spring.core.RocketMQTemplate")
@RocketMQTransactionListener
@Slf4j
public class CommonRocketMQLocalTransactionListener implements RocketMQLocalTransactionListener {

    private volatile static Map<String, RocketMQLocalTransactionService> rocketMQLocalTransactionServiceMap = null;
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String topic = msg.getHeaders().get(RocketMQHeaders.TOPIC,String.class);
        try{
            RocketMQLocalTransactionService rocketMQLocalTransactionService = getRocketMQLocalTransactionServiceMap(topic);
            rocketMQLocalTransactionService.executeLocalTransaction(msg,arg);
            log.info("topic:{},执行本地事务成功",topic);
            return RocketMQLocalTransactionState.COMMIT;
        }catch (Exception e){
            log.error("topic"+topic+"执行本地事务失败，消息回滚",e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String topic = msg.getHeaders().get(RocketMQHeaders.TOPIC,String.class);
        try{
            RocketMQLocalTransactionService rocketMQLocalTransactionService = getRocketMQLocalTransactionServiceMap(topic);
            boolean exists = rocketMQLocalTransactionService.checkLocalTransaction(msg);
            if(exists){
                log.info("topic:{},检查本地事务成功",topic);
                return RocketMQLocalTransactionState.COMMIT;
            }else{
                log.info("topic:{},不存在本地事务，回滚消息",topic);
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        }catch (Exception e){
            log.error("topic"+topic+"检查本地事务失败，消息回滚",e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    private RocketMQLocalTransactionService getRocketMQLocalTransactionServiceMap(String topic){
        if(rocketMQLocalTransactionServiceMap==null){
            synchronized (CommonRocketMQLocalTransactionListener.class){
                if(rocketMQLocalTransactionServiceMap==null){
                    rocketMQLocalTransactionServiceMap = MqApplicationContext.getBeansOfType(RocketMQLocalTransactionService.class)
                            .values().stream().collect(Collectors.toMap(RocketMQLocalTransactionService::getTopic, service->service));
                }
            }
        }
        RocketMQLocalTransactionService rocketMQLocalTransactionService = rocketMQLocalTransactionServiceMap.get(topic);
        return rocketMQLocalTransactionService;

    }

}
