package com.linkcircle.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@TableName("corp_user")
public class CorpUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 企业ID
     */
    private String corpId;
    /**
     * 用户ID
     */
    private Long userId;


}
