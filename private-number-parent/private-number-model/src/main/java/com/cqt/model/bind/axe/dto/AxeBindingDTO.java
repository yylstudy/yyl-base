package com.cqt.model.bind.axe.dto;

import com.cqt.model.common.CommonBinding;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * AXE模式绑定入参
 *
 * @author linshiqiang
 * @since 2021-10-18 15:09:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AxeBindingDTO extends CommonBinding implements Serializable {

    private static final long serialVersionUID = -86095877632173029L;

    /**
     * X绑定的真实被叫号码, 可以接受手机号码、固定电话。固话有区号且为全数字；如：18600008888或0108888999
     */
    @ApiModelProperty(value = "X绑定的真实被叫号码, 可以接受手机号码、固定电话。固话有区号且为全数字；如：18600008888或0108888999", example = "18600008888")
    @NotBlank(message = "tel 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String tel;

    /**
     * 指定X号码
     */
    @JsonProperty("tel_x")
    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String telX;

    /**
     * 指定分机号
     */
    @JsonProperty("tel_x_ext")
    @Length(max = 4)
    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String extNum;

    /**
     * 是否生成AYB绑定关系
     * 1: 是 (默认值)
     * 0: 否
     */
    @JsonProperty("ayb_flag")
    @ApiModelProperty(value = "是否生成AYB绑定关系, 1: 是 (默认值);  0: 否", example = "1")
    private Integer aybFlag;

    /**
     * AYB有效持续时间单位秒
     */
    @JsonProperty("ayb_expiration")
    @ApiModelProperty(value = "AYB有效持续时间单位秒", example = "1000")
    private Long aybExpiration;

    /**
     * 获取Y号码地区；默认=area_code
     */
    @JsonProperty("ayb_area_code")
    @ApiModelProperty(value = "获取Y号码地区；默认=area_code", example = "0591")
    private String aybAreaCode;

    /**
     * tel回呼Y时被叫看到的来电号码 1：看见Y   2：看见tel_x
     */
    @JsonProperty("ayb_other_show")
    @ApiModelProperty(value = "tel回呼Y时被叫看到的来电号码 1：看见Y   2：看见tel_x", example = "2")
    private Integer aybOtherShow;

    /**
     * AX模型B打给A，接通后B播放的语音
     */
    @ApiModelProperty(value = "AX模型B打给A，接通后B播放的语音")
    private String audio;

    /**
     * AX模型B打给A，接通后A播放的语音
     */
    @JsonProperty("audio_called")
    @ApiModelProperty(value = "AX模型B打给A，接通后A播放的语音")
    private String audioCalled;

    /**
     * AYB模型A打给B，接通后主叫A听到的提示音
     */
    @JsonAlias({"audio_a_call_x", "ayb_audio_a_call_x"})
    @JsonProperty("ayb_audio_a_call_x")
    @ApiModelProperty(value = "AYB模型A打给B，接通后主叫A听到的提示音")
    private String aybAudioACallX;

    /**
     * AYB模型B打给A，接通后主叫B听到的提示音
     */
    @JsonAlias({"audio_b_call_x", "ayb_audio_b_call_x"})
    @JsonProperty("ayb_audio_b_call_x")
    @ApiModelProperty(value = "AYB模型B打给A，接通后主叫B听到的提示音")
    private String aybAudioBCallX;

    /**
     * AYB模型B打给A，接通后A端听到的提示音
     */
    @JsonAlias({"audio_a_called_x", "ayb_audio_a_called_x"})
    @JsonProperty("ayb_audio_a_called_x")
    @ApiModelProperty(value = "AYB模型B打给A，接通后A端听到的提示音")
    private String aybAudioACalledX;

    /**
     * AYB模型A打给B，接通后B端听到的提示音
     */
    @JsonAlias({"audio_b_called_x", "ayb_audio_b_called_x"})
    @JsonProperty("ayb_audio_b_called_x")
    @ApiModelProperty(value = "AYB模型A打给B，接通后B端听到的提示音")
    private String aybAudioBCalledX;

    /**
     * 不区分模型，A打给B，接通前主叫A听到的提示音
     */
    @JsonProperty("ayb_audio_a_call_x_before")
    @ApiModelProperty(value = "不区分模型，A打给B，接通前主叫A听到的提示音")
    private String aybAudioACallXBefore;

    /**
     * 不区分模型，B打给A，接通前主叫B听到的提示音
     */
    @JsonProperty("ayb_audio_b_call_x_before")
    @ApiModelProperty(value = "不区分模型，B打给A，接通前主叫B听到的提示音")
    private String aybAudioBCallXBefore;

    @ApiModelProperty(hidden = true)
    private String vccId;

    @ApiModelProperty(hidden = true)
    private String cityCode;

    /**
     * tel是否可以回呼X找到最近联系人, 回呼有效时间为callback_expiration:
     * 1: 是
     * 0: 否(默认值)
     */
    @JsonProperty("callback_flag")
    @ApiModelProperty(value = "tel是否可以回呼X找到最近联系人, 回呼有效时间为callback_expiration:\n" +
            "1: 是\n" +
            "0: 否(默认值)\n")
    private Integer callbackFlag;

    /**
     * 回呼AX绑定有效持续时间(秒),
     * 仅当callback_flag=1时有效
     * 必须小于expiration
     * 为空默认与expiration相同
     */
    @JsonProperty("callback_expiration")
    @ApiModelProperty(value = "回呼AX绑定有效持续时间(秒),\n" +
            "仅当callback_flag=1时有效\n" +
            "必须小于expiration\n" +
            "为空默认与expiration相同\n")
    private Long callbackExpiration;

    /**
     * 是否随机分配一个固话号码phone
     * 1: 是
     * 0: 否(默认值)
     */
    @ApiModelProperty(value = "是否随机分配一个固话号码phone\n" +
            "1: 是\n" +
            "0: 否(默认值)\n")
    @JsonProperty("phone_flag")
    private Integer phoneFlag;

}
