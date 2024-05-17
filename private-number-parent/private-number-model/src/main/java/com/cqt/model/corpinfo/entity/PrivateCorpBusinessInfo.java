package com.cqt.model.corpinfo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/5/25 9:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateCorpBusinessInfo {

    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value = "企业id")
    private String vccId;

    @ApiModelProperty(value = "申请的绑定类型")
    private String businessType;

    @ApiModelProperty(value = "密钥")
    private String secretKey;

    @ApiModelProperty(value = "分机号个数")
    private Integer extNumCount;

    @ApiModelProperty(value = "客户绑定关系查询url")
    private String bindQueryUrl;

    @ApiModelProperty(value = "是否禁止发送短信")
    private Integer smsFlag;

    @ApiModelProperty(value = "企业话单是否推送")
    private Integer cdrPushFlag;

    @ApiModelProperty(value = "是否允许录音")
    private Integer recordFlag;

    @ApiModelProperty(value = "是否使用第三方")
    private Integer thirdFlag;

    @ApiModelProperty(value = "供应商Id")
    private String supplierId;

    @ApiModelProperty(value = "请输入分机号提示语")
    private String digitsIvr;

    @ApiModelProperty(value = "无绑定关系默认提示音")
    private String notBindIvr;


    @ApiModelProperty(value = "企业话单推送地址")
    private String billPushUrl;

    @ApiModelProperty(value = "话单重推间隔时间")
    private Integer pushRetryMin;

    @ApiModelProperty(value = "话单重推间隔次数")
    private Integer pushRetryNum;

    @ApiModelProperty(value = "企业短信推送地址")
    private String smsPushUrl;

    @ApiModelProperty(value = "通话状态推送地址")
    private String statusPushUrl;

    @ApiModelProperty(value = "通话状态重推次数")
    private Integer statusRetryNum;

    @ApiModelProperty(value = "通话状态重推间隔")
    private Integer statusRetryMin;

    @ApiModelProperty(value = "通话状态推送失败是否重试")
    private Integer statusPushRetryFlag;

    @ApiModelProperty(value = "通话状态推送标记")
    private Integer statusPushFlag;

    @ApiModelProperty(value = "解绑事件是否推送")
    private Integer unBindPushFlag;

    @ApiModelProperty(value = "解绑事件推送地址")
    private String unBindPushUrl;

    @ApiModelProperty(value = "AXEYB-AYB绑定推送地址")
    private String aybBindPushUrl;

    @ApiModelProperty(value = "绑定接口适配json")
    private String bindingParamAdapter;

    @ApiModelProperty(value = "绑定关系接口是否需要鉴权")
    private Integer authFlag;

    @ApiModelProperty(value = "有效期开始时间")
    private Date expireStartTime;

    @ApiModelProperty(value = "有效期结束时间")
    private Date expireEndTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

}
