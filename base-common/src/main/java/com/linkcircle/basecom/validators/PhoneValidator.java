package com.linkcircle.basecom.validators;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 手机格式校验器
 * @createTime 2023/5/26 15:47
 */

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private String[] regex;
    @Override
    public void initialize(Phone phone) {
        regex = phone.regexp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (!StringUtils.hasLength(value)) {
            return true;
        }
        return Arrays.stream(regex).anyMatch(regex-> Pattern.compile(regex).matcher(value).matches());
    }
}
