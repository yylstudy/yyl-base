package com.linkcircle.basecom.common;

import lombok.Data;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/27 19:01
 */
@Data
public class LoginUserInfo {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 姓名
     */
    private String username;

}
