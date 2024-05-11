package com.yyl;

import com.linkcircle.basecom.validators.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/26 9:13
 */
@AllArgsConstructor
@Getter
public enum SexEnum implements BaseEnum {
    MAN("1","男"),
    WOMAN("2","女");
    private final String code;
    private final String desc;

}
