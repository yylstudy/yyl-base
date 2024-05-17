package com.cqt.base.aspect;

import com.cqt.base.util.RpcContextUtil;
import com.cqt.base.util.TraceIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author linshiqiang
 * date:  2023-07-03 19:22
 */
@Slf4j
@Aspect
// @Component
@RequiredArgsConstructor
public class TraceIdAspect {

    @Pointcut("@annotation(com.cqt.base.aspect.Trace)")
    public void pointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed;
        try {
            String traceId = RpcContextUtil.getTraceId();
            TraceIdUtil.setTraceId(traceId);
            proceed = joinPoint.proceed();
        } finally {
            RpcContextUtil.clear();
            TraceIdUtil.remove();
        }

        return proceed;
    }
}
