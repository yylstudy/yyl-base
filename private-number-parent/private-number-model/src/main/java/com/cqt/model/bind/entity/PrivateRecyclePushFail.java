package com.cqt.model.bind.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/11/1 13:54
 */
@Data
public class PrivateRecyclePushFail {

    @TableId(type = IdType.INPUT)
    private String bindId;

    private String requestId;

    private String vccId;

    private String telA;

    private String telB;

    private String tel;

    private String telX;

    private String telY;

    @TableField("tel_b_other")
    private String otherTelB;

    private String extNum;

    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date expireTime;

    private String areaCode;

    private String numType;

    private String operateType;
}
