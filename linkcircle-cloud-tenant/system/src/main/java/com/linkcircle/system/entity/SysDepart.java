package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "sys_depart")
public class SysDepart extends BaseEntity {

    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门父级id
     */
    private Long parentId;

    /**
     * 企业ID
     */
    private String corpId;
    /**
     * 排序
     */
    private Integer sort;



}
