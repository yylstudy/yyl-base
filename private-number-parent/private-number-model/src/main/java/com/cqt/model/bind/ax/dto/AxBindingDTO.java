package com.cqt.model.bind.ax.dto;

import com.cqt.model.common.CommonBinding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * AX模式  号码绑定接口参数
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
public class AxBindingDTO extends CommonBinding implements Serializable {

    private static final long serialVersionUID = -8056276556567752106L;

    /**
     * A号码；18600008888或0108888999;
     */
    @JsonProperty("tel_a")
    @ApiModelProperty(value = "A号码；18600008888或0108888999;")
    @NotBlank(message = "tel_a 不能为空")
    private String telA;

    /**
     * A打给B，接通后A号码拨打X放音文件
     */
    @JsonProperty("audio_a_call_x")
    @ApiModelProperty(value = "A打给B，接通后A号码拨打X放音文件")
    private String audioACallX;

    /**
     * B打给A，接通后B号码拨打X放音文件
     */
    @JsonProperty("audio_b_call_x")
    @ApiModelProperty(value = "A打给B，接通后B号码拨打X放音文件")
    private String audioBCallX;

    /**
     * 非A、B 的其它号码拨打 X 放音文件 (不传时，需有默认放音文件）
     */
    @JsonProperty("audio_other_call_x")
    @ApiModelProperty(value = "非A、B 的其它号码拨打 X 放音文件 (不传时，需有默认放音文件）")
    private String audioOtherCallX;

    /**
     * B打给A，接通后A端听到的提示音
     */
    @JsonProperty("audio_a_called_x")
    @ApiModelProperty(value = "B打给A，接通后A端听到的提示音")
    private String audioACalledX;

    /**
     * A打给B，接通后B端听到的提示音
     */
    @JsonProperty("audio_b_called_x")
    @ApiModelProperty(value = "A打给B，接通后B端听到的提示音")
    private String audioBCalledX;

    /**
     * A打给B，接通前A号码拨打X放音文件
     */
    @JsonProperty("audio_a_call_x_before")
    @ApiModelProperty(value = " A打给B，接通前A号码拨打X放音文件")
    private String audioACallXBefore;

    /**
     * B打给A，接通前B号码拨打X放音文件
     */
    @JsonProperty("audio_b_call_x_before")
    @ApiModelProperty(value = " B打给A，接通前B号码拨打X放音文件")
    private String audioBCallXBefore;

    /**
     * 虚拟号码
     */
    @JsonProperty("tel_x")
    @ApiModelProperty(value = "指定已申请到的X号码进行绑定, 值不为空优先使用指定X号码, 否则根据area_code随机分配")
    private String telX;

    @ApiModelProperty(hidden = true)
    private String vccId;

    @ApiModelProperty(hidden = true)
    private String cityCode;

}
