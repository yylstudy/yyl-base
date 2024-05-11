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
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 字典ID
     */
    private Long dictId;
    /**
     * 编码
     */
    private String itemValue;
    /**
     * 名称
     */
    private String itemText;

    /**
     * 排序
     */
    private Integer sort;
}