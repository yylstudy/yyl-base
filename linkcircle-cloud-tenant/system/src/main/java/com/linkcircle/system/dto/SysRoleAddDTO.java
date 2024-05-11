package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysRoleAddDTO {

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @NotNull(message = "角色名称不能为空")
    @Size(min = 1, max = 20, message = "角色名称(1-20)个字符")
    private String roleName;

    @Schema(description = "角色编码")
    @NotNull(message = "角色编码 不能为空")
    @Size(min = 1, max = 20, message = "角色编码(1-20)个字符")
    private String roleCode;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述")
    @Size(max = 255, message = "角色描述最多255个字符")
    private String remark;


}
