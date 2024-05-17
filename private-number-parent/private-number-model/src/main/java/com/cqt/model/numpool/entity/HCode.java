package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2022/1/19 14:20
 */
@Data
@TableName("t_hcode")
public class HCode {

    @TableId
    @TableField("telcode")
    private String telCode;

    @TableField("areacode")
    private String areaCode;
}
