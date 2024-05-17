package com.cqt.queue.calltask.aspect;

import com.cqt.model.calltask.dto.CallTaskOperateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-03 19:22
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TaskInfoAspect {

    @Pointcut("execution(* com.cqt.queue.calltask.controller.OutBoundCallTaskController.*(..))")
    public void pointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed;
        try {
            TaskInfoContext.set((CallTaskOperateDTO) joinPoint.getArgs()[0]);
            proceed = joinPoint.proceed();
        } finally {
            TaskInfoContext.remove();
        }
        return proceed;
    }
}
