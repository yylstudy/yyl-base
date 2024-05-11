package com.linkcircle.mybatis.encrypt;

import com.linkcircle.mybatis.annotation.FieldEncrypt;

import java.lang.reflect.Field;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/11 18:49
 */

public class EnDecryptField {
    /**
     * 加密字段
     */
    private Field field;
    /**
     * 加密注解
     */
    private FieldEncrypt fieldEncryptAnnotation;

    public EnDecryptField(Field field, FieldEncrypt fieldEncryptAnnotation) {
        this.field = field;
        this.fieldEncryptAnnotation = fieldEncryptAnnotation;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public FieldEncrypt getFieldEncryptAnnotation() {
        return fieldEncryptAnnotation;
    }

    public void setFieldEncryptAnnotation(FieldEncrypt fieldEncryptAnnotation) {
        this.fieldEncryptAnnotation = fieldEncryptAnnotation;
    }
}
