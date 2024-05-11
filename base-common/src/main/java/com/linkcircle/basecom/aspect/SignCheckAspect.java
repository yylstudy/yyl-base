package com.linkcircle.basecom.aspect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkcircle.basecom.constants.CommonConstant;
import com.linkcircle.basecom.exception.BusinessException;
import com.linkcircle.basecom.handler.SignHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 操作日志切面
 *
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 签名校验，两种处理方式，一种是使用过滤器或者spring拦截器，复用流的方式解析获取body进行验签，但是这种有个问题是无法和注解搭配使用
 *              第二种是AOP切注解，但是这个时候无法根据HttpServletRequest获取body，因为流已经被读取过了，这里采用获取具体@RequestBody的注解的参数
 *              进行验签。
 *              验签参数：值为空或者null的不参与签名校验
 * @createTime 2024/1/10 17:11
 */
@Aspect
@Slf4j
public class SignCheckAspect {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static{
        //值为空不参与序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    @Value("${sign.expireSecond:300}")
    private long expireSecond;
    @Autowired
    private SignHandler signHandler;

    public SignCheckAspect(){
        log.info("初始化：{}完成============================",this.getClass().getSimpleName());
    }

    @Pointcut("@annotation(com.linkcircle.basecom.annotation.SignCheck)" +
            "||@within(com.linkcircle.basecom.annotation.SignCheck)")
    public void logPointCut() {
    }

    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        TreeMap<String, String> paramMap = new TreeMap<>();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String headerSign = request.getHeader(CommonConstant.X_SIGN);
        if(!StringUtils.hasText(headerSign)){
            throw new BusinessException("签名不能为空");
        }
        checkAndFillParamMap(request,paramMap);
        fillUrlParams(request,paramMap);
        fillBodyParams(request,joinPoint,paramMap);
        String sign = signHandler.sign(paramMap);
        log.info("paramMap:{}",paramMap);
        if(!Objects.equals(sign,headerSign)){
            throw new BusinessException("签名校验错误，请检查");
        }
    }

    /**
     * 检查和填充参数
     * @param request
     * @param paramMap
     */
    private void checkAndFillParamMap(HttpServletRequest request,TreeMap<String, String> paramMap){
        String timestampStr = request.getHeader(CommonConstant.X_TIMESTAMP);
        if(!StringUtils.hasText(timestampStr)){
            throw new BusinessException("时间戳不能为空");
        }
        long timestamp;
        try{
            timestamp = Long.parseLong(timestampStr);
        }catch (Exception e){
            throw new BusinessException("时间戳格式不正确");
        }
        long currentTimeStamp = System.currentTimeMillis();
        if(currentTimeStamp>(timestamp+expireSecond*1000)){
            throw new BusinessException("接口时间戳已过期");
        }
        String appKey = request.getHeader(CommonConstant.APP_KEY);
        if(!StringUtils.hasText(appKey)){
            throw new BusinessException("appKey不能为空");
        }
        signHandler.checkAppKey(appKey);
        paramMap.put(CommonConstant.X_TIMESTAMP,timestampStr);
        paramMap.put(CommonConstant.APP_KEY,appKey);
        paramMap.remove("_t");
    }
    /**
     * 填充url参数
     * @param request
     * @return
     */
    private void fillUrlParams(HttpServletRequest request,TreeMap<String, String> paramMap) {
        if (!StringUtils.hasText(request.getQueryString())) {
            return ;
        }
        String param = "";
        try {
            param = URLDecoder.decode(request.getQueryString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String[] params = param.split("&");
        for (String s : params) {
            int index = s.indexOf("=");
            paramMap.put(s.substring(0, index), s.substring(index + 1));
        }
    }
    /**
     * 填充body参数
     * @param request
     * @return
     */
    private void fillBodyParams(HttpServletRequest request,JoinPoint joinPoint,TreeMap<String, String> paramMap) {
        try{
            if (HttpMethod.GET.name().equals(request.getMethod())) {
                return ;
            }
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            Parameter[] parameters = signature.getMethod().getParameters();
            for(int i=0;i<parameters.length;i++){
                Parameter parameter = parameters[i];
                if(parameter.isAnnotationPresent(RequestBody.class)){
                    String agrStr = objectMapper.writeValueAsString(args[i]);
                    Map<String,String> map = objectMapper.readValue(agrStr,Map.class);
                    paramMap.putAll(map);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


}
