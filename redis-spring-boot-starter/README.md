#### 说明

双机房redis集成（底层使用jedis作为连接池，lettuce不适配k8s，

redisson连接池在RedisTemplate的Api中有bug（opsForList().leftPop会出现递归无法退出的情况））

#### 引用

```
<dependency>
    <groupId>com.linkcircle</groupId>
    <artifactId>redis-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

默认依赖

```
spring-boot-starter-aop  跟随spring boot版本
commons-pool2  
jedis  3.8.0  2.11.1
redisson-spring-boot-starter  3.22.0
```



#### 使用说明

##### redis双机房配置类

MultiRedisProperties



##### redis双机房工具类

RedisUtil



##### 异地机房redis操作类

Redis2Executor



##### 防止重复提交注解

spring配置类中开启此功能

```
@Configuration
@EnableRepeatSubmit
public class MyConfiguration {
}
```

目标方法上添加@RepeatSubmit注解，lockKey为锁粒度，支持spel表达式

keyAppendUserId方法为是否在lockKey后面添加userId，这个可以通过自定义userIdHandler实现

（可以配合common模块的LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();实现）

```
@RequestMapping("testRepeatSubmit")
@RepeatSubmit(lockKey = "testRepeatSubmit:#{#sysUser.id}",keyAppendUserId = false)
public String testRepeatSubmit(@RequestBody SysUser sysUser){
    try {
        Thread.sleep(10000);
        log.info("testRepeatSubmit");
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "success";
}
```

lockTime方法默认为5，重复提交的限制时间最多为5s，如果一个请求的耗时大于5s，那么在5s后，即使前一个请求还未结束，另一个请求也能调通，

##### 分布式锁注解

目标方法上添加@Lock注解

lockKey：锁粒度，支持spel表达式

expireSeconds：锁过期时间默认为30s

waitTime：等待时间，默认10s， -1 则表示一直等待

```
    @RequestMapping("testLock")
    @Lock(lockKey = "testLock:#{#sysUser.id}")
    public String testLock(@RequestBody SysUser sysUser){
        try {
            log.info("testLock");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }
```