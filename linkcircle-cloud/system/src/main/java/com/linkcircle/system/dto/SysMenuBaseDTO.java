package com.linkcircle.system.dto;

import com.linkcircle.basecom.validators.CheckEnum;
import com.linkcircle.system.common.MenuTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysMenuBaseDTO {

    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 30, message = "菜单名称最多30个字符")
    private String menuName;

    @CheckEnum(value = MenuTypeEnum.class, message = "菜单类型必须在指定范围{value}")
    private Integer menuType;

    @Schema(description = "父菜单ID 无上级可传0")
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "是否为外链")
    @NotNull(message = "是否为外链不能为空")
    private Boolean frameFlag;

    @Schema(description = "外链地址")
    private String frameUrl;

    @Schema(description = "是否缓存")
    @NotNull(message = "是否缓存不能为空")
    private Boolean cacheFlag;

    @Schema(description = "前端权限字符串")
    private String webPerms;

    @Schema(description = "后端端权限字符串")
    private String apiPerms;

    @Schema(description = "菜单图标")
    private String icon;
}
