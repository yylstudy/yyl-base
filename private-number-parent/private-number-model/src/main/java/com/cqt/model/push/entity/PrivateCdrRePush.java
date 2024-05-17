package com.cqt.model.push.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author CQT
 * 话单重推失败入库
 */
@Data
@TableName("private_cdr_repush")
public class PrivateCdrRePush implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;

    /**
     * 重推失败时间
     */
    @ApiModelProperty(value = "重推失败时间")
    private Date repushFailTime;

    /**
     * 企业VCCID
     */
    @ApiModelProperty(value = "企业VCCID")
    private String vccId;

    /**
     * 企业名称
     */
    @ApiModelProperty(value = "企业名称")
    private String vccName;

    /**
     * 话单推送url
     */
    @ApiModelProperty(value = "话单推送url")
    private String cdrPushUrl;

    /**
     * 失败原因
     */
    @ApiModelProperty(value = "失败原因")
    private String failReason;

    /**
     * json报文
     */
    @ApiModelProperty(value = "json报文")
    private String jsonStr;

    /**
     * createBy
     */
    @ApiModelProperty(value = "createBy")
    private String createBy;

    /**
     * updateBy
     */
    @ApiModelProperty(value = "updateBy")
    private String updateBy;

    /**
     * createTime
     */
    @ApiModelProperty(value = "createTime")
    private Date createTime;

    /**
     * updateTime
     */
    @ApiModelProperty(value = "updateTime")
    private Date updateTime;
}
