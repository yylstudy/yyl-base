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
    @FieldEncrypt
    @WebSensitive(strategy = com.linkcircle.basecom.desensitization.CurrencySensitiveStrategy.MobilePhone.class)
    private String dictCode;
    /**
     * 名称
     */
    @FieldSensitive(strategy = CurrencySensitiveStrategy.MobilePhone.class)
    private String dictName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 邮箱
     */
    @WebSensitive(strategy = com.linkcircle.basecom.desensitization.CurrencySensitiveStrategy.Email.class)
    private String email;

}