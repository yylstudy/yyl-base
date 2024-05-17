package com.cqt.cdr.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.cqt.cdr.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName cc_push_err
 */
@TableName(value = "cc_push_err")
@Data
@NoArgsConstructor
public class PushErr implements Serializable {
    @TableId(type = IdType.AUTO)
    private String id;

    private String reason;

    private String ip;

    private String state;

    private Date updatetime;

    private Date createtime = new Date();

    private String json;

    private String url;

    private String vccid;

    private String type;

    private Integer reqcount;

    private String reqstate;

    private static final long serialVersionUID = 1L;

    public static PushErr getPushErr(String json, String vccid,String url, String reason) {
        PushErr pushErr = new PushErr();
        pushErr.setJson(json);
        pushErr.setVccid(vccid);
        pushErr.setReason(reason);
        pushErr.setIp(StringUtil.getLocalIp());
        pushErr.setState("0");
        pushErr.setType("cdrpush");
        pushErr.setReqstate("0");
        pushErr.setReqcount(0);
        pushErr.setUrl(url);
        return pushErr;
    }
}