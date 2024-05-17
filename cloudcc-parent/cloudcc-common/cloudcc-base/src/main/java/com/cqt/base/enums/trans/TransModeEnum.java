package com.cqt.base.enums.trans;

import com.cqt.base.enums.BaseEnum;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-13 9:19
 * 转接模式
 * type  string  转接类型 1盲转 2咨询转  转接类型
 */
public enum TransModeEnum implements BaseEnum<TransModeEnum> {

    BLIND_TRANS(1, "盲转"),

    CONSULT_TRANS(2, "咨询转"),

    CANCEL_TRANS(3, "取消咨询转");

    private final Integer code;

    private final String name;

    TransModeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Optional<TransModeEnum> of(Integer code) {
        return Optional.ofNullable(BaseEnum.parseByCode(TransModeEnum.class, code));
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
