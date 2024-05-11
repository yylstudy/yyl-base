package com.linkcircle.redis.aspect;

import com.linkcircle.redis.annotation.RepeatSubmit;
import com.linkcircle.redis.config.RedisUtil;
import com.linkcircle.redis.config.UserIdHandler;
import com.linkcircle.redis.util.ParamParserHelp;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 防止重复提交分布式锁
 * @createTime 2024/1/30 16:37
 */
@Aspect
@Slf4j
public class RepeatSubmitAspect {
    @Autowired
    private UserIdHandler userIdHandler;
    @Autowired
    private RedisUtil redisUtil;

    @Pointcut("@annotation(repeatSubmit)")
    public void pointCut(RepeatSubmit repeatSubmit) {
    }
    @Around("pointCut(repeatSubmit)")
    public Object repeatSubmit(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        joinPoint.getSignature();
        String key = ParamParserHelp.getRealContent(repeatSubmit.lockKey(),method,args);
        if(repeatSubmit.keyAppendUserId()){
            String userId = userIdHandler.getUserId();
            key += ":"+userId;
        }
        long lockTime = repeatSubmit.lockTime();
        boolean needWait = repeatSubmit.needWait();
        boolean setSuccess = false;
        key = "repeatsubmit:"+key;
        log.info("repeatsubmit key:{}",key);
        try {
            setSuccess = redisUtil.setIfAbsent(key,1,lockTime);
            if(!setSuccess){
                throw new RuntimeException("请勿重复提交");
            }
            return joinPoint.proceed();
        } finally {
            if(setSuccess&&!needWait){
                redisUtil.del(key);
            }
        }
    }



}
