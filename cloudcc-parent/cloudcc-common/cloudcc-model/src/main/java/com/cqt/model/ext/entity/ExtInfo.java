package com.cqt.model.ext.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 分机信息
 *
 * @author Xienx
 * @date 2023-07-03 10:38:10:38
 */
@Data
@TableName("cloudcc_ext_info")
public class ExtInfo implements Serializable {

    private static final long serialVersionUID = -6140112972952735924L;

    /**
     * 完整分机号
     */
    @TableId(type = IdType.INPUT)
    private String sysExtId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 分机号
     */
    private String extId;

    /***
     * 分机密码
     * */
    private String password;

    /**
     * 分机通话模式（1：普通模式 2：长通模式）
     */
    private Integer callMode;
}
