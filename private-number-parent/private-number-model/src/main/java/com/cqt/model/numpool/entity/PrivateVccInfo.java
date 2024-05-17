package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;


/**
 * @author hlx
 * @date 2022-02-22
 */
@Data
public class PrivateVccInfo {

    /**
     * 企业id
     */
    @TableId(type = IdType.INPUT)
    private String vccId;

    private String vccName;

    /**
     * 秘钥
     */
    private String secretKey;

    /**
     * 开通的绑定类型
     */
    @ApiModelProperty(example = "\\.*")
    private String numType;

    /**
     * 分机号个数
     */
    @ApiModelProperty(example = "10000")
    private Integer extNumCount;

    /**
     * 客户 绑定关系查询url
     */
    private String bindQueryUrl;

    /**
     * 内部 绑定关系结果处理url
     */
    private String bindConvertUrl;

    /**
     * 该企业的通用主池号码数量
     */
    @ApiModelProperty(example = "200")
    private Integer masterNum;

    /**
     * 企业话单推送的url
     */
    private String billPushUrl;

    /**
     * 企业短信推送地址
     * */
    private String smsPushUrl;

    /**
     * 通话状态推送的url
     */
    private String statusPushUrl;

    /**
     *  通话状态事件是否推送  判断该字符串是否含有事件标记
     *  callin callout ringing answer hangup
     */
    private String statusPushFlag;

    /**
     * 解绑推送url
     */
    private String unBindPushUrl;

    /**
     * AXE-AYB绑定推送url
     */
    private String aybBindPushUrl;

    /**
     * 是否禁止发短信 1 是, 0 否
     */
    @ApiModelProperty(example = "0")
    private Integer smsFlag;

    /**
     * 是否允许录音 1 是 0 否
     */
    @ApiModelProperty(example = "0")
    private Integer recordFlag;

    /**
     * 无绑定关系默认提示音
     */
    private String notBindIvr;

    /**
     * 请输入分机号提示语
     */
    private String digitsIvr;

    /**
     * 绑定接口适配json
     * {
     *   "AXB": {
     *      "通用字段": "定制字段"
     *   }
     * }
     */
    private String bindingParamAdapter;

    /**
     * 绑定接口适配map
     */
    @TableField(exist = false)
    private Map<String, Map<String, String>> bindingParamAdapterMap;

    /**
     * 绑定关系接口是否需要鉴权 1是, 其他否, 默认 1
     */
    @ApiModelProperty(example = "1")
    private Integer authFlag;

}
