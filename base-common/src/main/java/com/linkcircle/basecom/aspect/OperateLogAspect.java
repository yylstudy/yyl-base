package com.linkcircle.basecom.aspect;

import cn.hutool.extra.servlet.ServletUtil;
import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.config.ThreadPool;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.basecom.handler.OperateLogHandler;
import com.linkcircle.basecom.util.ParamParserHelp;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 操作日志切面
 *
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/1/10 17:11
 */
@Aspect
@Slf4j
public class OperateLogAspect {
    @Autowired
    private OperateLogHandler operateLogHandler;

    public OperateLogAspect(){
        log.info("初始化：{}完成============================",this.getClass().getSimpleName());
    }

    @Pointcut("@annotation(com.linkcircle.basecom.annotation.OperateLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        try {
            Object result = joinPoint.proceed();
            saveLog(result,null,start,method,args);
            return result;
        } catch (Exception e) {
            saveLog(null,e,start,method,args);
            throw e;
        }
    }

    private void saveLog(Object result,Exception e,long start,Method method,Object[] args){
        try{
            long costTime = System.currentTimeMillis()-start;
            boolean isSuccess = true;
            String failReason = "";
            OperateLog operateLog = method.getAnnotation(OperateLog.class);
            String content = ParamParserHelp.getRealContent(operateLog.content(), method, args);
            String operateMethod = method.getDeclaringClass().getName()+"."+method.getName();
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String requestUrl = "";
            String ip = ServletUtil.getClientIP(request);
            if(request!=null){
                requestUrl = request.getRequestURL().toString();
            }
            if(e!=null){
                failReason = e.getMessage();
                isSuccess = false;
            }
            if(result instanceof Result
                    && !((Result<?>) result).isSuccess()){
                failReason = ((Result<?>) result).getMessage();
                isSuccess = false;
            }
            LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();
            boolean finalIsSuccess = isSuccess;
            String finalFailReason = failReason;
            String finalRequestUrl = requestUrl;
            ThreadPool.execute(()-> operateLogHandler.addLog(request,finalIsSuccess,finalFailReason,content,
                    finalRequestUrl,operateMethod,costTime, loginUserInfo,ip));
        }catch (Exception ee){
            log.error("插入日志失败",ee);
        }
    }
}
