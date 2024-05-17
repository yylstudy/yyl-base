package com.cqt.model.bind.axg.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.cqt.model.common.CommonBinding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * AXG模式  号码绑定接口参数
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
public class AxgBindingDTO extends CommonBinding implements Serializable {

    private static final long serialVersionUID = -8056276556567752106L;

    /**
     * A号码；18600008888或0108888999;
     */
    @JsonProperty("tel_a")
    @ApiModelProperty(value = "A号码；18600008888或0108888999;")
    @NotBlank(message = "tel_a 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String telA;

    /**
     * B号码；18600008888或0108888999,
     */
    @JsonProperty("tel_b")
    @ApiModelProperty(value = "B号码, 多个已逗号隔开")
    @NotBlank(message = "tel_b 不能为空")
//    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String telB;

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

    @ApiModelProperty(hidden = true)
    private String cityCode;

    @ApiModelProperty(hidden = true)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    @ApiModelProperty(hidden = true)
    private String vccId;

}
