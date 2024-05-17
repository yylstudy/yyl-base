package com.cqt.broadnet.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date:  2023-05-15 14:45
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ApiAuthAspect {

    private static final String SIGN_METHOD = "sign_method";

    private static final String SIGN = "sign";

    private static final String APP_KEY = "app_key";


    private final PrivateNumberBindProperties privateNumberBindProperties;

    @Pointcut("@annotation(com.cqt.broadnet.config.Auth)")
    public void pointcut() {
    }

    @Around("pointcut()")
    @SuppressWarnings("all")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (privateNumberBindProperties.getAuth()) {
            LinkedHashMap<String, String> arg = (LinkedHashMap) args[0];
            checkSign(arg);
        }
        return joinPoint.proceed();
    }

    private void checkSign(Map<String, String> params) {
        String signMethod = params.get(SIGN_METHOD);
        String sign = params.get(SIGN);
        String appKey = params.get(APP_KEY);
        if (StrUtil.isEmpty(appKey) || !privateNumberBindProperties.getAppKey().equals(appKey)) {
            throw new ApiAuthException("app_key校验不通过!");
        }
        TreeMap<String, Object> treeMap = new TreeMap<>(params);
        treeMap.remove(SIGN);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            builder.append(entry.getKey()).append(entry.getValue());
        }
        String paramsStr = builder.toString();
        String secretKey = privateNumberBindProperties.getSecretKey();
        String checkSign;
        if ("hmac".equals(signMethod)) {
            HMac hmac = SecureUtil.hmac(HmacAlgorithm.HmacMD5, secretKey);
            checkSign = hmac.digestHex(paramsStr).toUpperCase();
        } else {
            String md5String = secretKey + paramsStr + secretKey;
            checkSign = SecureUtil.md5(md5String).toUpperCase();
        }

        if (StrUtil.isEmpty(sign) || !sign.equals(checkSign)) {
            throw new ApiAuthException("sign校验不通过!");
        }
    }
}
