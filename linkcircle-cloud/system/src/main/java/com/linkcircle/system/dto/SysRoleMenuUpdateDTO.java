package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysRoleMenuUpdateDTO {

    /**
     * 角色id
     */
    @Schema(description = "角色id")
    @NotNull(message = "角色id不能为空")
    private Long roleId;

    /**
     * 菜单ID 集合
     */
    @Schema(description = "菜单ID集合")
    @NotNull(message = "菜单ID不能为空")
    private List<Long> menuIdList;

}
