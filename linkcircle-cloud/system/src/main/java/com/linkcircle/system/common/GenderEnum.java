package com.linkcircle.system.common;

import com.linkcircle.basecom.validators.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@AllArgsConstructor
@Getter
public enum GenderEnum implements BaseEnum {

    /**
     * 0 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男 1 奇数为阳
     */
    MAN(1, "男"),

    /**
     * 女 2 偶数为阴
     */
    WOMAN(2, "女");

    private final Integer code;

    private final String desc;
}
