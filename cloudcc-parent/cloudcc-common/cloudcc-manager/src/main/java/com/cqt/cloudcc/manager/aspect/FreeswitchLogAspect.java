package com.cqt.cloudcc.manager.aspect;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.exception.BaseReqException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 19:22
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FreeswitchLogAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("execution(* com.cqt.feign.freeswitch.FreeswitchApiFeignClient.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        if (log.isInfoEnabled()) {
            log.info("[底层接口-{}] 请求参数: {}", methodName, objectMapper.writeValueAsString(args));
        }
        long start = System.currentTimeMillis();
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Exception e) {
            log.error("[底层接口-{}] 调用失败: ", methodName, e);
            throw new BaseReqException(SdkErrCode.BASE_REQUEST_ERROR);
        } finally {
            if (log.isInfoEnabled()) {
                log.info("[底层接口-{}] 耗时: {}ms, 请求结果: {}",
                        methodName, (System.currentTimeMillis() - start),
                        Objects.nonNull(proceed) ? objectMapper.writeValueAsString(proceed) : null);
            }
        }

        return proceed;
    }
}
