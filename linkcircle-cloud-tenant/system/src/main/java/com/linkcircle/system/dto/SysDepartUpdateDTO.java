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
public class SysDepartUpdateDTO extends SysDepartAddDTO {

    @Schema(description = "部门id")
    @NotNull(message = "部门id不能为空")
    private Long id;
    @Schema(description = "企业ID")
    @NotNull(message = "企业ID不能为空")
    private String corpId;
}
