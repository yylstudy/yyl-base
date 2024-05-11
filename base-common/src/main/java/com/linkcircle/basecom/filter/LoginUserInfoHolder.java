package com.linkcircle.basecom.filter;


import com.linkcircle.basecom.common.LoginUserInfo;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/29 13:55
 */

public class LoginUserInfoHolder {

    public static final ThreadLocal<LoginUserInfo> TOKEN_USER_THREAD_LOCAL = new ThreadLocal();
    /**
     * 设置登录用户信息
     * @param tokenUser
     */
    public static <T extends LoginUserInfo> void set(T tokenUser){
        TOKEN_USER_THREAD_LOCAL.set(tokenUser);
    }
    /**
     * 获取登录用户信息
     */
    public static <T extends LoginUserInfo> T get(){
        return (T)TOKEN_USER_THREAD_LOCAL.get();
    }
    /**
     * 清除登录用户信息
     */
    public static void remove(){
        TOKEN_USER_THREAD_LOCAL.remove();
    }
}
