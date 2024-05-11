package com.linkcircle.basecom.validators;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 基础枚举类，需要参数交换的枚举类需要实现此接口
 * @createTime 2023/5/26 15:54
 */

public interface BaseEnum {
    /**
     * 枚举值，主要是基础类型
     * @return
     */
    Object getCode();
    /**
     * 枚举描述
     * @return
     */
    String getDesc();
}
