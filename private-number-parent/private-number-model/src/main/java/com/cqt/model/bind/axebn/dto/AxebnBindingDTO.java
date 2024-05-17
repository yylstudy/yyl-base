package com.cqt.model.bind.axebn.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * AXEBN模式绑定关系(MtkfBindInfoAxebn)实体类
 *
 * @author linshiqiang
 * @since 2021-10-18 15:09:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AxebnBindingDTO extends BaseAuth implements Serializable {

    private static final long serialVersionUID = 444550212677779088L;

    /**
     * 企业每个请求Id唯一，如果是同一个请求重复提交，则Id保持相同
     */
    @JsonProperty("request_id")
    @ApiModelProperty(value = "请求ID,request_id相同的请求，须返回相同的结果。", example = "1xxxx104fb10e6ab3dde05c7d6aae7077a1cf8")
    @NotBlank(message = "request_id 不能为空")
    private String requestId;

    /**
     * A号码
     */
    @JsonProperty("tel_a")
    @ApiModelProperty(value = "A号码", example = "18600008888")
    @NotBlank(message = "tel_a 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "tel_a must number")
    private String telA;

    /**
     * B号码
     */
    @JsonProperty("tel_b")
    @ApiModelProperty(value = "B号码", example = "18600008888")
    @NotBlank(message = "tel_b 不能为空")
//    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String telB;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    @JsonProperty("area_code")
    @ApiModelProperty(value = "以0开头的虚拟号区号（如010）", example = "0591")
    @NotBlank(message = "area_code 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "area_code must number")
    private String areaCode;

    /**
     * 使用全国池, 1 是, 0 否
     */
    @JsonProperty("whole_area")
    @ApiModelProperty(value = " 是否使用全国池, 1 是, 默认0 否")
    private Integer wholeArea = 0;

    /**
     * 有效持续时间，即过expiration秒后AX关系失效自动解绑；
     */
    @ApiModelProperty(value = "有效持续时间，即过expiration ms后AXB关系失效自动解绑；", example = "1000")
    @NotNull(message = "expiration 不能为空")
    @Min(value = 1, message = "expiration must >= 0")
    @Max(value = Integer.MAX_VALUE, message = "expiration must <= 2147483647")
    private Long expiration;

    /**
     * B打给A，接通后主叫B听到的提示音
     */
    @ApiModelProperty(value = "B打给A，接通后主叫B听到的提示音")
    private String audio;

    /**
     * B打给A，接通后被叫A端听到的提示音
     */
    @JsonProperty("audio_called")
    @ApiModelProperty(value = "B打给A，接通后被叫A端听到的提示音")
    private String audioCalled;

    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要（数值字符串）
     */
    @ApiModelProperty(value = "是否录音 1 是, 0 否")
    @JsonProperty("enable_record")
    private Integer enableRecord;

    /**
     * 是否禁用短信
     * 0：正常(可不传，系统默认0)  1：禁用短信
     */
    @ApiModelProperty(value = "是否禁用短信 0：正常(可不传，系统默认0)  1：禁用短信")
    private Integer type;

    @JsonProperty("user_data")
    @ApiModelProperty(value = "业务侧信息透传字段。所有当前bind_id产生的通话在话单回推时需要将该字段带回")
    private String userData;

    @ApiModelProperty(hidden = true)
    private String vccId;

    @ApiModelProperty(hidden = true)
    private String cityCode;

}
