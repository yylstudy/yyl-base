package com.cqt.base.aspect;

import java.lang.annotation.*;

/**
 * @author linshiqiang
 * date:  2023-08-09 11:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Trace {

}
