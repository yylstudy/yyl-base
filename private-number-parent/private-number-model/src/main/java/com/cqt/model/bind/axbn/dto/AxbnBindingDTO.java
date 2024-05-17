package com.cqt.model.bind.axbn.dto;

import com.cqt.model.common.CommonBinding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * AXBN模式  号码绑定接口参数
 *
 * @author linshiqiang
 * @since 2021-11-02 14:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AxbnBindingDTO extends CommonBinding implements Serializable {

    private static final long serialVersionUID = -8876375376219533743L;

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
    @ApiModelProperty(value = "B号码；18600008888或0108888999,")
    @NotBlank(message = "tel_b 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "must number")
    private String telB;

    /**
     * 其他B号码；号码可为最多为5个号码，中间以英文逗号分隔
     */
    @JsonProperty("tel_b_other")
    @ApiModelProperty(value = "其他B号码；号码可为最多为5个号码，中间以英文逗号分隔")
    @Pattern(regexp = "^[\\d,]*$", message = "must number")
    private String otherTelB;

    /**
     * A拨打Y放音文件
     */
    @JsonProperty("audio_a_call_x")
    @ApiModelProperty(value = "A拨打Y放音文件")
    private String audioACallX;

    /**
     * B拨打X放音文件编码
     */
    @JsonProperty("audio_b_call_x")
    @ApiModelProperty(value = "B拨打X放音文件编码")
    private String audioBCallX;

    @JsonProperty("mode")
    @ApiModelProperty(value = "mode")
    @NotNull(message = "mode 不能为空")
    private Integer mode;

    @ApiModelProperty(hidden = true)
    private String vccId;

    @ApiModelProperty(hidden = true)
    private String cityCode;
}
