package com.linkcircle.basecom.annotation;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 签名校验
 * @createTime 2024/3/27 11:03
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignCheck {

}
