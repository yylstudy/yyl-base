package com.linkcircle.system.common;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
public class RedisKeyFormat {
    /**
     * 验证码
     */
    public static final String CAPTCHA = "system:captcha:%s";
    public static final String SEPARATOR = ":";
    /**
     * 登录
     */
    public static final String LOGIN_TOKEN = "system:login:%s";
    /**
     * 字典
     */
    public static final String DICT_CACHE = "system:cache:dict:%s";


    public static String getCaptchaKey(String id){
        return String.format(CAPTCHA,id);
    }
    public static String getLoginTokenKey(String token){
        return String.format(LOGIN_TOKEN,token);
    }

    public static String getDictKey(String dictCode){
        return String.format(DICT_CACHE,dictCode);
    }
}
