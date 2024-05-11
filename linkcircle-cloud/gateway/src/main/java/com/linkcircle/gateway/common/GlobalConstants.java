package com.linkcircle.gateway.common;

/**
* @Description: GlobalConstants
* @author: scott
* @date: 2020/01/01 16:01
*/
public class GlobalConstants {

    /**
     * 签名字符串
     */
    public static final String X_SIGN = "X-Sign";
    /**
     * 时间戳
     */
    public static final String X_TIMESTAMP = "X-TIMESTAMP";
    /**
     * app key
     */
    public static final String APP_KEY = "APP-KEY";
    /**
     * app key
     */
    public static final String APP_PREFIX = "APP:";
    /**
     * 请求成功标识
     */
    public static final Integer SC_OK_200 = 200;
    /**
     * token为空提示语
     */
    public static final String TOKEN_IS_EMPTY = "token为空，请重新登录";
    /**
     * header中的token
     */
    public final static String X_ACCESS_TOKEN = "X-Access-Token";
    /**
     * token验证服务名
     */
    public final static String TOKEN_SERVER_NAME = "lb://system";
    /**
     * token验证服务名
     */
    public final static String TOKEN_VERIFY_URL = "tokenVerify";
    /**
     * token参数
     */
    public final static String TOKEN_PARAM = "token";
}
