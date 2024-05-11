package com.yyl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.linkcircle.basecom.annotation.WebSensitive;
import com.linkcircle.basecom.entity.BaseEntity;
import com.linkcircle.mybatis.annotation.FieldEncrypt;
import com.linkcircle.mybatis.annotation.FieldSensitive;
import com.linkcircle.mybatis.sensitive.CurrencySensitiveStrategy;
import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@TableName("test")
public class Test  {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private Date createTime;
}