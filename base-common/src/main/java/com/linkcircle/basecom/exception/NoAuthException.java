package com.linkcircle.basecom.exception;


import cn.hutool.http.HttpStatus;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
public class NoAuthException extends RuntimeException {

    private int code = HttpStatus.HTTP_UNAUTHORIZED;

    public int getCode() {
        return code;
    }
}
