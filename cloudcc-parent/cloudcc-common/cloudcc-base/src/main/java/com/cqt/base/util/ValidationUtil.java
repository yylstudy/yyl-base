package com.cqt.base.util;

import com.cqt.base.exception.ParamsException;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author linshiqiang
 * date 2023-09-01 20:47:00
 */
public class ValidationUtil {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 验证数据
     *
     * @param object 数据
     */
    public static void validate(Object object) throws ParamsException {

        Set<ConstraintViolation<Object>> validate = VALIDATOR.validate(object);

        // 验证结果异常
        throwParamException(validate);
    }

    /**
     * 验证数据(分组)
     *
     * @param object 数据
     * @param groups 所在组
     */
    public static void validate(Object object, Class<?>... groups) throws ParamsException {

        Set<ConstraintViolation<Object>> validate = VALIDATOR.validate(object, groups);

        // 验证结果异常
        throwParamException(validate);
    }

    /**
     * 验证数据中的某个字段(分组)
     *
     * @param object       数据
     * @param propertyName 字段名称
     */
    public static void validate(Object object, String propertyName) throws ParamsException {
        Set<ConstraintViolation<Object>> validate = VALIDATOR.validateProperty(object, propertyName);

        // 验证结果异常
        throwParamException(validate);

    }

    /**
     * 验证数据中的某个字段(分组)
     *
     * @param object       数据
     * @param propertyName 字段名称
     * @param groups       所在组
     */
    public static void validate(Object object, String propertyName, Class<?>... groups) throws ParamsException {

        Set<ConstraintViolation<Object>> validate = VALIDATOR.validateProperty(object, propertyName, groups);

        // 验证结果异常
        throwParamException(validate);

    }

    /**
     * 验证结果异常
     *
     * @param validate 验证结果
     */
    private static void throwParamException(Set<ConstraintViolation<Object>> validate) throws ParamsException {
        if (!CollectionUtils.isEmpty(validate)) {
            for (ConstraintViolation<Object> next : validate) {
                if(next != null && next.getMessage() != null) {
                    throw new ParamsException(next.getMessage());
                }
            }
        }
    }

}
