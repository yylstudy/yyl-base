package com.linkcircle.system.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.linkcircle.basecom.config.ApplicationContextHolder;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.entity.SysUser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/27 18:27
 */

public class JwtUtil {
    public static int DEFAULT_TOKEN_EXPIRE_MINUTE = ApplicationContextHolder.getEnvironment()
            .getProperty(CommonConstant.TOKEN_EXPIRE_CONFIG,Integer.class,CommonConstant.DEFAULT_TOKEN_EXPIRE_MINUTE);

    public static String DEFAULT_SECRET = "DEFAULT_SECRET";
    /**
     * 生成token
     * @param sysUser
     * @return
     */
    public static String sign(SysUser sysUser) {
        DateTime now = DateTime.now();
        DateTime expTime = now.offsetNew(DateField.MINUTE, DEFAULT_TOKEN_EXPIRE_MINUTE);
        Map<String, Object> payload = new HashMap<>();
        payload.put("username",sysUser.getUsername());
        payload.put("phone",sysUser.getPhone());
        payload.put("id",sysUser.getId());
        payload.put(JWTPayload.ISSUED_AT, now);
        payload.put(JWTPayload.NOT_BEFORE, now);
        payload.put(JWTPayload.EXPIRES_AT, expTime);
        return JWTUtil.createToken(payload,DEFAULT_SECRET.getBytes());
    }

}
