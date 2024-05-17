package com.cqt.hmyc.web.x.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-06-07 16:44:16:44
 */
@Data
public class XModelQueryDTO implements Serializable {

    private static final long serialVersionUID = 7615408026188397812L;

    /**
     * 用于请求重试去重，每一组绑定关系该参数
     * 具有唯一性，同一组绑定关系多次重试该参
     * 数取值相同,最大不超过 20 位；
     */
    @ApiModelProperty(value = "请求唯一标识", required = true)
    private String requestId;

    /**
     * 分配给合作伙伴的 KEY，和鉴权参数中
     * platformid 一致。
     */
    @ApiModelProperty(value = "分配给合作伙伴的 KEY，和鉴权参数中platformid 一致。", required = true)
    private String vendorKey;

    /**
     * 主叫号码
     */
    @ApiModelProperty(value = "主叫号码", example = "13311112222", required = true)
    private String callNo;

    /**
     * 中间号码
     */
    @ApiModelProperty(value = "中间号码", example = "13803111232", required = true)
    private String secretNo;

    /**
     * 通话类型 0：呼叫；1：短信
     */
    @ApiModelProperty(value = "通话类型 0：呼叫；1：短信", example = "0", required = true)
    private String recordType;

    /**
     * 呼叫时间 格式：YYYYmmddHHMMSS
     * 如：20170905110000
     */
    @ApiModelProperty(value = "呼叫时间 格式：YYYYmmddHHMMSS如：20170905110000", required = true)
    private String callTime;

    /**
     * 呼叫Id 标识唯一一次呼叫
     */
    @ApiModelProperty(value = "呼叫id， 标识唯一呼叫", required = true)
    private String callId;

    /**
     * 分机号（opType=2后会传）
     */
    @ApiModelProperty(value = "分机号（opType=2后会传）")
    private String extensionNo;
}
