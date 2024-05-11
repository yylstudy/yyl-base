package com.linkcircle.mybatis.sensitive;

import com.linkcircle.mybatis.annotation.FieldSensitive;

import java.lang.reflect.Field;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/11 18:49
 */

public class SensitiveField {
    /**
     * 加密字段
     */
    private Field field;
    /**
     * 加密注解
     */
    private FieldSensitive fieldSensitive;

    public SensitiveField(Field field, FieldSensitive fieldSensitive) {
        this.field = field;
        this.fieldSensitive = fieldSensitive;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public FieldSensitive getFieldSensitive() {
        return fieldSensitive;
    }

    public void setFieldSensitive(FieldSensitive fieldSensitive) {
        this.fieldSensitive = fieldSensitive;
    }
}
