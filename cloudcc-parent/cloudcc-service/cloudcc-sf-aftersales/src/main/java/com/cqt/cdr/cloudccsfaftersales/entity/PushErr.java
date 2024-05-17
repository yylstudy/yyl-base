package com.cqt.cdr.cloudccsfaftersales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.cqt.cdr.cloudccsfaftersales.util.StringUtil;
import lombok.Data;

/**
 * @TableName cc_push_err
 */
@TableName(value = "cc_push_err")
@Data
public class PushErr implements Serializable {
    private Integer id;

    private String reason;

    private String ip;

    private String state;

    private Date updatetime;

    private Date createtime;

    private String json;

    private String url;

    private String vccid;

    private String type;

    private Integer reqcount;

    private String reqstate;

    private static final long serialVersionUID = 1L;

    public static PushErr getPushErr(String json, String vccid, String url, String reason, String type) {
        PushErr pushErr = new PushErr();
        pushErr.setJson(json);
        pushErr.setVccid(vccid);
        pushErr.setReason(reason);
        pushErr.setIp(StringUtil.getLocalIp());
        pushErr.setCreatetime(new Date());
        pushErr.setUpdatetime(new Date());
        pushErr.setState("0");
        pushErr.setType(type);
        pushErr.setReqstate("0");
        pushErr.setReqcount(0);
        pushErr.setUrl(url);
        return pushErr;
    }
}