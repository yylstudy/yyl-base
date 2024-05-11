package com.linkcircle.basecom.annotation;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 字典注解
 * @createTime 2024/3/26 13:37
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dict {
    /**
     * 字符编码
     * @return
     */
    String dictCode();

    /**
     * 翻译的字典字段，这个用于@AutoDict翻译时指定key
     * @return
     */
    String dictText() default "";
}
