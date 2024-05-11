package com.linkcircle.system.common;

import com.linkcircle.basecom.common.LoginUserInfo;
import lombok.Data;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/27 19:01
 */
@Data
public class SystemLoginUserInfo extends LoginUserInfo {
    /**
     * 邮箱
     */
    private String email;
    /**
     * 当前登录企业
     */
    private String corpId;
    /**
     * 部门ID
     */
    private String departId;

}
