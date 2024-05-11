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
public class SysDepartUserTreeDTO extends SysDepartDTO {

    @Schema(description = "部门用户列表")
    private List<SysUserDTO> employees;

    @Schema(description = "子部门")
    private List<SysDepartUserTreeDTO> children;

}
