package com.cqt.cdr.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName sys_dict
 */
@TableName(value ="sys_dict")
@Data
@DS("ms")
public class Dict implements Serializable {
    private String id;

    private String dictName;

    private String dictCode;

    private String description;

    private Integer delFlag;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private Integer type;

    private String tenantId;

    private String lowAppId;

    private static final long serialVersionUID = 1L;
}