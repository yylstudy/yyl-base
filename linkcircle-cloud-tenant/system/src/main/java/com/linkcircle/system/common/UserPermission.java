package com.linkcircle.system.common;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */

@Data
public class UserPermission {

    /**
     * 权限列表
     */
    private List<String> permissionList;

    /**
     * 角色列表
     */
    private List<String> roleList;


}
