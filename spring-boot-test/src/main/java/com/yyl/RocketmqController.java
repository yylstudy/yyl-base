package com.yyl;

import com.linkcircle.basecom.config.ApplicationContextHolder;
import com.linkcircle.mq.common.RocketmqLocalTransactionState;
import com.linkcircle.mq.common.RocketmqSendCallback;
import com.linkcircle.mq.producer.MqProducer;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/25 15:22
 */
@RestController
@Slf4j
@DependsOn("applicationContextHolder")
public class RocketmqController {
    @Autowired
    private MqProducer mqProducer;
    @RequestMapping("send1")
    public void send1(){
        SysUser sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        //正常消息
        mqProducer.asyncSendMessage("normal_exchange:aaa",sysUser);
        String key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        mqProducer.asyncSendMessage("normal_exchange:aaa",sysUser,key);
        //延迟消息
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        mqProducer.asyncSendDelayMessage("delay_exchange:aaa",sysUser,4);
        key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        mqProducer.asyncSendDelayMessage("delay_exchange:aaa",sysUser,4,key);
//        //过期消息
//        mqProducer.convertAndAsyncSendExpireMessage("expire_exchange:aaa",UUID.randomUUID(),30000);
//        key = UUID.randomUUID().toString();
//        log.info("key:{}",key);
//        mqProducer.convertAndAsyncSendExpireMessage("expire_exchange:aaa",UUID.randomUUID(),30000,key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        boolean result = mqProducer.syncSendMessage("normal_exchange:aaa",sysUser);
        log.info("消息同步发送成功：{}",result);
        key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        result = mqProducer.syncSendMessage("normal_exchange:aaa",sysUser,key);
        log.info("消息同步发送成功：{}",result);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        result = mqProducer.syncSendDelayMessage("delay_exchange:aaa",sysUser,4);
        log.info("消息同步发送成功：{}",result);
        key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        result = mqProducer.syncSendDelayMessage("delay_exchange:aaa",sysUser,4,key);
        log.info("消息同步发送成功：{}",result);
        //发送事务消息
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl");
        sysUser.setSex(UUID.randomUUID().toString());
        RocketmqLocalTransactionState localTransactionState = mqProducer.sendTransactionMessage("transaction_exchange:aaa",sysUser,key,sysUser);
        log.info("事务消息发送结果：{}",localTransactionState);
        //回调消息
        key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        mqProducer.asyncSendMessage("normal_exchange:aaa", sysUser, null, key, new RocketmqSendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("sendResult:{}",e);
            }
        });
        key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        mqProducer.asyncSendMessage("delay_exchange:aaa", sysUser, 4, key, new RocketmqSendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("sendResult:{}",e);
            }
        });
    }
    @PostConstruct
    public void init(){
        ExecutorService pool = Executors.newFixedThreadPool(8);
        RocketMQTemplate rocketMQTemplate = ApplicationContextHolder.getBean(RocketMQTemplate.class);
        rocketMQTemplate.setAsyncSenderExecutor(pool);
    }

    AtomicLong atomicLong = new AtomicLong();
    @RequestMapping("send4")
    public void send4() throws Exception{
        for(int i=0;i<500000;i++){
            SysUser sysUser = new SysUser();
            sysUser.setUsernme("yyl");
            sysUser.setSex(UUID.randomUUID().toString());
            //正常消息
            mqProducer.asyncSendMessage("normal_exchange:aaa",sysUser,null, UUID.randomUUID().toString(),new RocketmqSendCallback(){
                @Override
                public void onSuccess(SendResult sendResult) {
                    if(atomicLong.incrementAndGet()%500==0){

                    }
                    log.info("send4 发送消息成功：{}",sendResult.getSendStatus());
                }
                @Override
                public void onException(Throwable e) {
                    log.error("发送失败",e);
                }
            });
        }
    }

    @RequestMapping("send5")
    public void send5() throws Exception{
        RocketMQTemplate rocketMQTemplate = ApplicationContextHolder.getBean(RocketMQTemplate.class);
        for(int i=0;i<500000;i++){
            SysUser sysUser = new SysUser();
            sysUser.setUsernme("yyl");
            sysUser.setSex(UUID.randomUUID().toString());
            //正常消息
            rocketMQTemplate.asyncSend("normal_exchange:aaa",sysUser,new SendCallback(){
                @Override
                public void onSuccess(SendResult sendResult) {
                    if(atomicLong.incrementAndGet()%500==0){

                    }
                    log.info("send5 发送消息成功：{}",sendResult.getSendStatus());
                }
                @Override
                public void onException(Throwable e) {
                    log.error("发送失败",e);
                }
            },600000);
        }
    }

}
