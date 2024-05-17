package com.cqt.model.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * @author linshiqiang
 * @date 2022/2/15 15:29
 */
@Data
public class CommonBinding {

    @ApiModelProperty(value = "appkey", example = "173", hidden = true)
    private String appkey;

    @ApiModelProperty(value = "时间戳", example = "1634786279204")
    private Long ts;

    @ApiModelProperty(value = "签名", example = "1234567890")
    private String sign;

    /**
     * 指示呼叫转接的录音格式，仅下列值有效。默认是 wav。
     * mp3
     * wav
     */
    @ApiModelProperty(value = "指示呼叫转接的录音格式，仅下列值有效。默认是 wav。\n" +
            "    mp3\n" +
            "    wav", example = "wav")
    @JsonProperty("record_file_format")
    private String recordFileFormat;

    /**
     * 在被叫上显示来话的真实号码。
     * 2：显示X号码(默认值)
     * 3：显示真实号码
     */
    @ApiModelProperty(value = "在被叫上显示来话的真实号码。\n" +
            "1：系统随机分配虚号Y \n" +
            "2：显示X号码(默认值)\n" +
            "3：显示真实号码", example = "2")
    private Integer model;

    /**
     * 录音模式, 默认0
     * 0：分声道录音
     * 1：按主被叫分文件录音
     */
    @ApiModelProperty(value = "录音方式。\n" +
            " 0：混音，即通话双方的声音混合在一个声道中。\n" +
            "  1：双声道，即通话双方的声音分别录制在左、右两个声道中。\n" +
            "如果不携带该参数，参数值默认为0。", example = "0")
    @JsonProperty("record_mode")
    private Integer recordMode;

    @ApiModelProperty(value = "录音方式。\n" +
            " 0：混音，即通话双方的声音混合在一个声道中。\n" +
            "  1：双声道，即通话双方的声音分别录制在左、右两个声道中。\n" +
            "如果不携带该参数，参数值默认为0。", example = "0")
    @JsonProperty("dual_record_mode")
    private Integer dualRecordMode;

    @JsonProperty("user_data")
    @ApiModelProperty(value = "业务侧信息透传字段。所有当前bind_id产生的通话在话单回推时需要将该字段带回")
    private String userData;

    @JsonAlias({"", "whole_area"})
    @JsonProperty("whole_area")
    @ApiModelProperty(value = " 是否使用全国池, 1 是, 默认0 否", example = "0")
    private Integer wholeArea;

    @JsonProperty("enable_record")
    @ApiModelProperty(value = "是否录音 1 是 0 否", example = "1")
    private Integer enableRecord;

    /**
     * 0：正常(可不传，系统默认0)  1：禁用短信
     */
    @ApiModelProperty(value = "是否禁用短信, 0：正常(可不传，系统默认0)  1：禁用短信", example = "0")
    private Integer type;

    /**
     * 有效持续时间，即过expiration ms后AXB关系失效自动解绑；
     */
    @JsonProperty("expiration")
    @ApiModelProperty(value = "有效持续时间，即过expiration ms后AXB关系失效自动解绑；", example = "3600")
    @NotNull(message = "expiration 不能为空")
    @Min(value = 1, message = "expiration must >= 1")
    @Max(value = Integer.MAX_VALUE, message = "expiration must <= 2147483640")
    private Long expiration;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    @JsonProperty("area_code")
    @ApiModelProperty(value = " 以0开头的虚拟号区号（如010）", example = "0591")
    @NotEmpty(message = "area_code 不能为空")
    private String areaCode;

    /**
     * 企业每个请求Id唯一，如果是同一个请求重复提交，则Id保持相同
     */
    @JsonProperty("request_id")
    @ApiModelProperty(value = "请求ID,request_id相同的请求，须返回相同的结果。", example = "a02fc61fa22c4d00a64a8950f5aada7f")
    @NotBlank(message = "request_id 不能为空")
    private String requestId;

    /**
     * 地市编码匹配规则(默认2)
     * 1: 地市池不足, 分配全国号码池
     * 2: 地市池不足, 不分配全国号码池
     */
    @JsonProperty("area_match_mode")
    @ApiModelProperty(value = "地市编码匹配规则, 1: 地市池不足, 分配全国号码池; 2: 地市池不足, 不分配全国号码池", example = "2")
    private Integer areaMatchMode;

    /**
     * 最大通话时长
     */
    @JsonProperty("max_duration")
    @ApiModelProperty(value = "最大通话时长, 默认最大7200s", example = "3600")
    @Min(value = 60, message = "max_duration must >= 60")
    @Max(value = 7200, message = "max_duration must <= 7200")
    private Integer maxDuration;

    /**
     * 通话最后一分钟放音
     */
    @JsonProperty("last_min_voice")
    @ApiModelProperty(value = "通话最后一分钟放音")
    private String lastMinVoice;
}
