package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.linkcircle.basecom.entity.BaseEntity;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 登录密码
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门id
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long departId;

    /**
     * 是否被禁用 0否1是
     */
    private Boolean disabledFlag;

    /**
     * 是否删除0否 1是
     */
    private Boolean deletedFlag;

    /**
     * 备注
     */
    private String remark;


}
