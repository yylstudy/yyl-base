package com.cqt.model.corpinfo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * date 2022/5/25 9:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateCorpBusinessInfoDTO {

    @ApiModelProperty(value = "企业id")
    private String vccId;

    private String vccName;

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

    @ApiModelProperty(value = "请输入分机号提示语")
    private String digitsIvr;

    @ApiModelProperty(value = "无绑定关系默认提示音")
    private String notBindIvr;

    @ApiModelProperty(value = "企业话单推送地址")
    private String billPushUrl;

    @ApiModelProperty(value = "话单是否重推")
    private Integer pushRetryFlag;

    @ApiModelProperty(value = "话单重推间隔时间")
    private Integer pushRetryMin;

    @ApiModelProperty(value = "话单重推间隔次数")
    private Integer pushRetryNum;

    @ApiModelProperty(value = "企业短信推送地址")
    private String smsPushUrl;

    @ApiModelProperty(value = "通话状态推送地址")
    private String statusPushUrl;

    @ApiModelProperty(value = "通话状态推送失败是否重试")
    private Integer statusPushRetryFlag;

    @ApiModelProperty(value = "通话状态推送标记")
    private Integer statusPushFlag;

    @ApiModelProperty(value = "解绑事件推送地址")
    private String unBindPushUrl;

    @ApiModelProperty(value = "解绑事件是否推送")
    private Integer unBindPushFlag;

    @ApiModelProperty(value = "AXE-AYB绑定推送地址")
    private String aybBindPushUrl;

    @ApiModelProperty(value = "绑定接口适配json")
    private String bindingParamAdapter;

    /**
     * 绑定接口适配map
     */
    @TableField(exist = false)
    private Map<String, Map<String, String>> bindingParamAdapterMap;

    @ApiModelProperty(value = "绑定关系接口是否需要鉴权")
    private Integer authFlag;

    /**
     * 企业有效期 开始时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireStartTime;

    /**
     * 企业有效期 结束时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireEndTime;

    /**
     * 企业, 调用第三方供应商接口的地市编码
     * {
     * "1007:0593": "111"
     * }
     *
     * @see PrivateCorpBusinessInfoDTO#supplierStrategy
     */
    @Deprecated
    private Map<String, String> thirdAreaCode;

    /**
     * key: 企业id+地市
     * value: 供应商和权重
     *
     * @since 2.2.0
     */
    private Map<String, List<SupplierWeight>> supplierStrategy;

    @Deprecated
    private String supplierId;

    /**
     * 异常号码状态推送接口, 客户提供
     * TODO 该字段暂定
     */
    private String exceptionNoSyncUrl;

}
