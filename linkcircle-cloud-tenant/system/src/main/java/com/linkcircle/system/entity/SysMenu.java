package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.linkcircle.basecom.entity.BaseEntity;
import com.linkcircle.system.common.MenuScopeEnum;
import lombok.Data;
import com.linkcircle.system.common.MenuTypeEnum;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@TableName(value = "sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 菜单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 类型
     *
     * @see MenuTypeEnum
     */
    private Integer menuType;
    /**
     * 菜单范围
     * @see MenuScopeEnum
     */
    private Long menuScope;
    /**
     * 业务
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String business;
    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 是否为外链
     */
    private Boolean frameFlag;

    /**
     * 外链地址
     */
    private String frameUrl;

    /**
     * 是否缓存
     */
    private Boolean cacheFlag;

    /**
     * 显示状态
     */
    private Boolean visibleFlag;

    /**
     * 禁用状态
     */
    private Boolean disabledFlag;

    /**
     * 后端权限字符串
     */
    private String apiPerms;

    /**
     * 前端权限字符串
     */
    private String webPerms;

    /**
     * 菜单图标
     */
    private String icon;

}
