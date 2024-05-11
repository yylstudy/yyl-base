package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserRoleUpdateDTO {

    @Schema(description = "角色id")
    @NotNull(message = "角色id不能为空")
    protected Long roleId;

    @Schema(description = "用户id集合")
    @NotEmpty(message = "用户id不能为空")
    protected List<Long> userIdList;

}
