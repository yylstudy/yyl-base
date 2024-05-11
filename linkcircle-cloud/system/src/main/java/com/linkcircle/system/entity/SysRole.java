package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.linkcircle.basecom.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色备注
     */
    private String remark;

}
