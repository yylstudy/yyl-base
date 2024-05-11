package com.linkcircle.system.common;


import com.linkcircle.basecom.validators.BaseEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
public enum MenuScopeEnum implements BaseEnum {
    COMMON(1, "公共"),
    /**
     * 菜单
     */
    BUSINESS_COMMON(2, "业务"),
    /**
     * 普通菜单
     */
    OTHER(3, "普通");

    private final long code;

    private final String desc;

    private static Map<Long,MenuScopeEnum> map = Arrays.stream(MenuScopeEnum.values())
            .collect(Collectors.toMap(MenuScopeEnum::getCode, Function.identity()));

    MenuScopeEnum(long code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MenuScopeEnum getMenuScopeEnum(long value){
        return map.get(value);
    }

    @Override
    public Long getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
