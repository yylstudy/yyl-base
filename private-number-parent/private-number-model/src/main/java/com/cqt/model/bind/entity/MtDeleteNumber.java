package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/11/27 16:14
 */
@Data
@TableName("mt_delete_num")
public class MtDeleteNumber {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String areaCode;

    private String tel;

    private Date deleteTime;

}
