package com.linkcircle.system.common;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
public class CommonConstant {
    /**
     * 空密码字符串
     */
    public static final String EMPTY_PASSWORD = "";
    /**
     * 验证码有效期
     */
    public static final long CAPTCHA_EXPIRE_SECOND = 65L;
    /**
     * 用户重置默认密码
     */
    public static final String DEFAULT_PASSWORD = "cqt@1234";
    /**
     * 顶级菜单父菜单ID
     */
    public static final long TOP_MENU_PATENT_ID = 0L;
    /**
     * 顶级部门父部门ID
     */
    public static final long TOP_DEPART_PATENT_ID = 0L;
    /**
     * header中token的key
     */
    public static final String STRING_EMPTY = "";

    /**
     * 密码正则校验
     */
    public static final String PWD_REGEXP = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$";
    /**
     * token过期时间配置
     */
    public static final String TOKEN_EXPIRE_CONFIG = "jwt.expireMinute";
    /**
     * 默认token过期时长
     */
    public static final int DEFAULT_TOKEN_EXPIRE_MINUTE = 300;
    /**
     * 字典缓存超时时间，单位秒，默认6小时
     */
    public static final int DICT_EXPIRE_SECOND = 21600;
}
