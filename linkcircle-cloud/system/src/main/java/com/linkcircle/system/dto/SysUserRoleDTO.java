package com.linkcircle.system.dto;

import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserRoleDTO {

    private Long roleId;

    private Long userId;

    private String roleName;
}
