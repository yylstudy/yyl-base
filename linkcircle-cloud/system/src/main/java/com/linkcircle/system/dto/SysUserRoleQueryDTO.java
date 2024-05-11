package com.linkcircle.system.dto;

import com.linkcircle.basecom.page.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserRoleQueryDTO extends PageParam {

    @Schema(description = "关键字")
    private String username;

    @Schema(description = "角色id")
    private String roleId;
}
