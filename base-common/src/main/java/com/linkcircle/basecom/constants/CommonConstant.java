package com.linkcircle.basecom.constants;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
public class CommonConstant {
    /**
     * 默认的邮箱正则
     */
    public static final String DEFAULT_EMAIL_REGEX = "^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$";
    /**
     * 默认的手机正则
     */
    public static final String DEFAULT_MOBILE_REGEX = "^(?:(?:\\+|00)86)?1(?:(?:3[\\d])|(?:4[0,1,4-9])|(?:5[0-3,5-9])|(?:6[2,5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[0-3,5-9]))\\d{8}$";
    /**
     * 请求成功标识
     */
    public static final Integer SC_OK_200 = 200;
    /**
     * header中token的key
     */
    public static final String TOKEN_HEADER_KEY_CONFIG = "tokenHeaderKey";
    /**
     * header中token的key
     */
    public static final String TOKEN_HEADER_KEY = "X-Access-Token";
    /**
     * 时间戳
     */
    public static final String X_TIMESTAMP = "X-TIMESTAMP";
    /**
     * app key
     */
    public static final String APP_KEY = "X-APP-KEY";
    /**
     * 签名字符串
     */
    public static final String X_SIGN = "X-Sign";
    /**
     * 默认字典缓存前缀
     */
    public static final String DEFAULT_DICT_CACHE_PREFIX = "system:cache:dict:";
    /**
     * 默认字段翻译后缀
     */
    public static final String DICT_TEXT_SUFFIX = "_dictText";

}
