package com.cqt.monitor.web.callevent.xxjob;

import cn.hutool.http.Method;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * XXL-JOB 登录参数
 *
 * @author scott
 * @date 2022年07月05日 13:54
 */
@Data
@Builder
public class LoginParam implements Serializable {

    public static final String URI = "login";
    public static final Method METHOD = Method.POST;
    private static final long serialVersionUID = -2611504003602407062L;

    /**
     * XXL-JOB的登录用户名
     */
    private String userName;

    /**
     * XXL-JOB的登录密码
     */
    private String password;
}
