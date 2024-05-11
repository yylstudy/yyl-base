package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysMenuUpdateDTO extends SysMenuBaseDTO {

    @Schema(description = "菜单ID")
    @NotNull(message = "菜单ID不能为空")
    private Long id;
}
