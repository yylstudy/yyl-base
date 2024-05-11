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
     * 企业ID
     */
    private String corpId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 是否企业管理员角色
     */
    private boolean corpAdmin;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色备注
     */
    private String remark;

}
