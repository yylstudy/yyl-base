package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserBatchUpdateDepartDTO {

    @Schema(description = "用户id")
    @NotEmpty(message = "用户id不能为空")
    @Size(max = 99, message = "一次最多调整99个用户")
    private List<Long> userIdList;

    @Schema(description = "部门ID")
    @NotNull(message = "部门ID不能为空")
    private Long departId;
}
