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
public class SysMenuTreeDTO extends SysMenuDTO {

    @Schema(description = "菜单子集")
    private List<SysMenuTreeDTO> children;
}
