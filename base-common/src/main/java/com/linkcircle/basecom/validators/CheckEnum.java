package com.linkcircle.basecom.validators;



import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface CheckEnum {
    /**
     * 校验的枚举值
     * @return
     */
    Class<? extends BaseEnum> value();

    /**
     * 描述
     * @return
     */
    String message() default "必须在指定范围 {value}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
