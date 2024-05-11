package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 参数key
     */
    @TableField("`key`")
    private String key;

    /**
     * 参数的值
     */
    private String value;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

}
