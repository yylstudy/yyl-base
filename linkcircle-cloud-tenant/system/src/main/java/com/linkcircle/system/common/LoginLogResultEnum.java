package com.linkcircle.system.common;

import com.linkcircle.basecom.validators.BaseEnum;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
public enum LoginLogResultEnum implements BaseEnum {
    
    LOGIN_SUCCESS(0, "登录成功"),
    LOGIN_FAIL(1, "登录失败"),
    LOGIN_OUT(2, "退出登录");

    private Integer code;
    private String desc;

    LoginLogResultEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
