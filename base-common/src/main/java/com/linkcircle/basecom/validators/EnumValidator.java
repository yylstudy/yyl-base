package com.linkcircle.basecom.validators;

import cn.hutool.core.util.ObjectUtil;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.validationcontext.ExecutableValidationContext;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description ConstraintValidator 是线程安全的，所以可以将注解作为全局变量
 * @createTime 2023/5/26 15:51
 */

public class EnumValidator implements ConstraintValidator<CheckEnum, Object> {
    /**
     * 允许的枚举值
     */
    private List<Object> allowValues = new ArrayList<>();
    @Override
    public void initialize(CheckEnum constraintAnnotation) {
        Class<? extends BaseEnum> enumClass = constraintAnnotation.value();
        allowValues = Stream.of(enumClass.getEnumConstants()).map(BaseEnum::getCode)
                .collect(Collectors.toList());
    }
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if(ObjectUtil.isEmpty(value)){
            return true;
        }
        boolean match;
        if (value instanceof List) {
            // 如果为 List 集合数据
            match = this.checkList((List<Object>) value);
        }else{
            match = allowValues.contains(value);
        }
        if(match){
            return true;
        }else{
            context.disableDefaultConstraintViolation();
            String allValueStr = allowValues.stream().map(String::valueOf).collect(Collectors.joining(","));
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()
                    .replaceAll("\\{value}", allValueStr)).addConstraintViolation();
            return false;
        }
    }

    private boolean checkList(List<Object> list) {
        // 校验是否重复
        long count = list.stream().distinct().count();
        if (count != list.size()) {
            return false;
        }
        return allowValues.containsAll(list);
    }
}
