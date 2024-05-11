package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/14 17:46
 */
@Data
public class SysRoleUserQueryReqDTO extends SysUserQueryReqDTO {
    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
