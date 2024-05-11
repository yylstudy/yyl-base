package com.yyl;

import com.linkcircle.basecom.common.LoginUserInfo;
import lombok.Data;
import lombok.ToString;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/26 15:34
 */
@Data
@ToString(callSuper = true)
public class MyLoginUserInfo extends LoginUserInfo {
    private String sex;
}
