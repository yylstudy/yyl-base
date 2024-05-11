package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.linkcircle.basecom.entity.BaseEntity;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@TableName("corp")
public class Corp extends BaseEntity {
    /**
     * 企业编码
     */
    @TableId(type = IdType.INPUT)
    private String id;
    /**
     * 企业名称
     */
    private String name;


}
