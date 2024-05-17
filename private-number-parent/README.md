# 通用小号服务 private-number-parent

- private-number-parent 父工程
    - private-number-model 实体类
    - private-number-common 通用工具类
    - private-number-service 微服务父工程
        - private-number-gateway 绑定转发服务(gateway)
        - private-number-hmyc 号码隐藏-绑定关系增删改查服务
        - private-number-hmyc-third 第三方供应商hmyc（和多号）
        - private-number-hmyc-third-broadnet 对接广电
        - private-number-hmyc-third-chinanet 对接扬州电信
        - private-number-third-unicom 对接联通总部接口服务
        - private-number-wechat-push 微信视频号话单推送
        - private-number-qinghai-ali 对接阿里
            - private-number-qinghai-ali-hmyc 对接阿里-查询绑定关系
            - private-number-qinghai-ali-push 对接阿里-话单推送
            - private-number-qinghai-ali-sms 对接阿里-短信
        - private-number-hmyc-recycle 号码隐藏-号码回收服务
        - private-number-monitor 监控服务
        - private-number-push 话单推送服务
        - private-number-sms 短信服务
        - private-number-iccpsmspush 短信话单入库
        - private-number-hmbc 号码拨测
        - private-number-agent freeswitch 代理服务(命令执行, mq广播消息消费...)
        - private-number-vccidhmyc
    - private-number-starter 中间件starter
        - private-number-starter-rabbitmq
        - private-number-starter-redis
        - private-number-starter-xxljob
    - private-number-cloud-api feign接口

## 中间件

#### 双机房redis

```xml

<dependency>
    <groupId>com.cqt</groupId>
    <artifactId>private-number-starter-redis</artifactId>
</dependency>
```

#### xxljob定时任务

```xml

<dependency>
    <groupId>com.cqt</groupId>
    <artifactId>private-number-starter-xxljob</artifactId>
</dependency>

```

##### 自动注册

```yaml
xxljob:
  accessToken:
  adminAddresses: http://172.16.251.53:38080/xxl-job-admin/
  #adminAddresses: 
  logRetentionDays: 30
  logPath: /home/smp/xxl-job/${spring.application.name}
  address:
  ip:
  port: 58512
  appName: ${spring.application.name}
  title: 隐私号平台-基础配置
  userName: admin
  password: cqt!010@Xxljob
```

```java
@XxlJobRegister(jobDesc = "定时任务描述中文",
        cron = "0 0 1 * * ? *",
        triggerStatus = 1,
        executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND)
@XxlJob("job")
public void job(){

        }
```

#### rabbitmq

```xml

<dependency>
    <groupId>com.cqt</groupId>
    <artifactId>private-number-starter-rabbitmq</artifactId>
</dependency>
```

# 打包

```shell
mvn clean package -am -pl private-number-service/private-number-third-unicom
mvn clean package -am -pl private-number-service/private-number-hmyc

mvn clean package -am -pl private-number-service/private-number-gateway

```
