package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysRoleMenuTreeDTO {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "菜单列表")
    private List<SysMenuSimpleTreeDTO> menuTreeList;

    @Schema(description = "选中的菜单ID")
    private List<Long> selectedMenuId;
}
