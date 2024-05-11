#### 说明

mq连接基础组件，适配rabbitmq和rocketmq

#### 引用

```
<dependency>
    <groupId>com.linkcircle</groupId>
    <artifactId>mq-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

再根据项目使用情况引入rocketmq-spring-boot-starter或者spring-boot-starter-amqp

#### 配置文件

和rocketmq-spring-boot-starter或者spring-boot-starter-amqp的配置完全相同

#### 使用说明

##### 消息发送

spring中依赖注入MqProducer，其中提供了发送消息的方法

```
asyncSendMessage  异步发送消息
asyncSendDelayMessage  异步发送延迟消息
asyncSendExpireMessage  发送带有过期时间的消息 仅rabbitmq支持
syncSendMessage  同步发送消息 仅rocketmq支持
syncSendDelayMessage 同步发送延迟消息，仅rocketmq支持
sendTransactionMessage  发送事务消息 仅rocketmq支持
syncSendOrderlyMessage  同步发送顺序消息 仅rocketmq支持
asyncSendOrderlyMessage 异步发送顺序消息 仅rocketmq支持
```

发送事务消息时，需要实现RocketMQLocalTransactionService，例如

```
@Component
@Slf4j
@ConditionalOnClass(name = "org.apache.rocketmq.client.MQAdmin")
public class MyRocketMQLocalTransactionService implements RocketMQLocalTransactionService {
    @Override
    public String getTopic() {
        return "transaction_exchange";
    }

    @Override
    public void executeLocalTransaction(Message msg, Object arg) {
        log.info("agr:{}",arg);
        log.info("执行本地事务");
    }

    @Override
    public boolean checkLocalTransaction(Message msg) {
        log.info("agr:{}",msg.getPayload());
        log.info("检查本地事务");
        return true;
    }
}
```



##### 消息消费

类上添加注解MqMessageListener，并且实现MqListener接口，

```
@MqMessageListener(target = "dead_letter_queue")
@Slf4j
@Component
public class RabbitMqListener implements MqListener<SysUser> {
    @Override
    public void onMessage(SysUser sysUser) {
        log.info("dead_letter_queue 接收到消息：{}",sysUser);
    }
}
```

方法内，不抛异常，默认是ack