package com.cqt.model.bind.axebn.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author linshiqiang
 * @date 2022/3/9 16:34
 * AXEBN 追加tel_b
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxebnAppendTelDTO extends BaseAuth {

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定id", example = "cqt-axebn059215014658583916912648970290")
    private String bindId;

    @ApiModelProperty(value = "追加的B号码", example = "1363222")
    @JsonProperty("tel_b")
    private String telB;

    @ApiModelProperty(hidden = true)
    private String vccId;


}
