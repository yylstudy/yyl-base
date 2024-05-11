package com.linkcircle.basecom.validators;

import com.linkcircle.basecom.constants.CommonConstant;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/5/26 15:39
 */
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PhoneValidator.class
)
public @interface Phone {
    String[] regexp() default CommonConstant.DEFAULT_MOBILE_REGEX;
    String message() default "手机格式不正确";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
