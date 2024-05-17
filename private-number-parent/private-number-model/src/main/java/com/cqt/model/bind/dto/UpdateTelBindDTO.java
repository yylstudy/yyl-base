package com.cqt.model.bind.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/10/21 11:36
 * 更新已绑定号码
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTelBindDTO extends BaseAuth implements Serializable {

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定时返回的绑定ID")
    @NotBlank(message = "bindId 不能为空")
    private String bindId;

    @JsonProperty("tel_binded")
    @ApiModelProperty(value = "原绑定号码；18600008888或0108888999,只能为单个号码。")
    @NotBlank(message = "tel_binded 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "telBinded must be number")
    private String telBinded;

    @JsonProperty("tel_update")
    @ApiModelProperty(value = "需要更新号码；18600008888或0108888999,只能为单个号码。")
    @NotBlank(message = "tel_update 不能为空")
    @Pattern(regexp = "^[0-9]*$", message = "telUpdate must be number")
    private String telUpdate;

    @JsonProperty("vcc_id")
    @ApiModelProperty(value = "企业id", hidden = true)
    private String vccId;
}
