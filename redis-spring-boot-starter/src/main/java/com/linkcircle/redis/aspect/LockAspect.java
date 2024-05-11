package com.linkcircle.redis.aspect;

import com.linkcircle.redis.util.ParamParserHelp;
import com.linkcircle.redis.annotation.Lock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description redis分布式锁切面
 * @createTime 2024/3/27 18:05
 */
@Slf4j
@Aspect
@Component
public class LockAspect {
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable{
        Object obj = null;
        RLock rLock = getLock(joinPoint, lock);
        boolean getLock = false;
        //获取超时时间
        long expireSeconds = lock.expireSeconds();
        //等待多久,n秒内获取不到锁，则直接返回
        long waitTime = lock.waitTime();
        //执行aop
        if (rLock != null) {
            try {
                if (waitTime == -1) {
                    getLock = true;
                    //一直等待加锁
                    rLock.lock(expireSeconds, TimeUnit.MILLISECONDS);
                } else {
                    getLock = rLock.tryLock(waitTime, expireSeconds, TimeUnit.MILLISECONDS);
                }
                if (getLock) {
                    obj = joinPoint.proceed();
                } else {
                    log.error("获取锁:"+rLock.getName()+"异常");
                    throw new RuntimeException("获取锁:"+ rLock.getName() +"异常");
                }
            } finally {
                if (getLock) {
                    rLock.unlock();
                }
            }
        }
        return obj;
    }

    private RLock getLock(ProceedingJoinPoint joinPoint, Lock lock) {
        String key = lock.lockKey();
        if (!StringUtils.hasText(key)) {
            throw new RuntimeException("keys不能为空");
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        key = ParamParserHelp.getRealContent(key,method,args);
        log.info("lock key:{}",key);
        RLock rLock = redissonClient.getLock(key);
        return rLock;
    }
}
