package com.cqt.broadnet.config;

import java.lang.annotation.*;

/**
 * @author linshiqiang
 * date:  2023-05-15 15:11
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auth {
}
