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
 * @TableName sys_dict_item
 */
@TableName(value ="sys_dict_item")
@Data
@DS("ms")
public class DictItem implements Serializable {
    private String id;

    private String dictId;

    private String itemText;

    private String itemValue;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}