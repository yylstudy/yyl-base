package com.cqt.model.bind.ax.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/9 15:10
 * AX 设置tel_b
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetUpTelDTO extends BaseAuth implements Serializable  {

    private static final long serialVersionUID = -3099109269119152038L;

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定时返回的绑定ID")
    @NotBlank(message = "bindId 不能为空")
    private String bindId;

    @JsonProperty("vcc_id")
    @ApiModelProperty(value = "企业id", hidden = true)
    private String vccId;

    @JsonProperty("tel_b")
    @ApiModelProperty(value = "B号码")
    @NotBlank(message = "tel_b 不能为空")
    private String telB;
}
