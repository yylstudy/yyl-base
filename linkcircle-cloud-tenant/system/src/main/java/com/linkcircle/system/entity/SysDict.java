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
@TableName("sys_dict")
public class SysDict extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 编码
     */
    private String dictCode;
    /**
     * 名称
     */
    private String dictName;

    /**
     * 备注
     */
    private String remark;

}