package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date 2022/1/24 17:25
 * 操作类型
 */
@Getter
public enum OperateTypeEnum {

    /**
     * 新增
     */
    INSERT,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 设置绑定
     */
    BINDING,

    /**
     * 解绑
     */
    UNBIND,

    /**
     * 修改有效期
     */
    UPDATE_EXPIRE,

    /**
     * 修改号码
     */
    UPDATE_TEL,

    /**
     * 追加号码
     */
    APPEND_TEL,

    /**
     * 删除
     */
    DELETE,

    /**
     * 查询
     */
    QUERY
}
