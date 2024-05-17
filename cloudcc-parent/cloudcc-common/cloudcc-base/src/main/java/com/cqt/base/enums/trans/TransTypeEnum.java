package com.cqt.base.enums.trans;

import com.cqt.base.enums.BaseEnum;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-13 9:19
 * 转接人员类型
 * trans_type  string  是  转接人员: 1 坐席 2 技能 3 ivr 4 外线 5 满意度
 */
public enum TransTypeEnum implements BaseEnum<TransTypeEnum> {

    TRANS_AGENT(1, "坐席"),

    TRANS_SKILL(2, "技能"),

    TRANS_IVR(3, "ivr"),

    TRANS_OUT_LINE(4, "外线"),

    TRANS_SATISFACTION(5, "满意度");

    private final Integer code;

    private final String name;

    TransTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Optional<TransTypeEnum> of(Integer code) {
        return Optional.ofNullable(BaseEnum.parseByCode(TransTypeEnum.class, code));
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
