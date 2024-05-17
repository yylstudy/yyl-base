package com.cqt.model.bind.axebn.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;


/**
 * @author linshiqiang
 * @date 2022/3/9 15:54
 * AXEBN 绑定关系查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxebnBindQueryDTO extends BaseAuth {

    /**
     * 企业每个请求Id唯一，
     */
    @JsonProperty("request_id")
    @ApiModelProperty(value = "请求ID,request_id相同的请求，须返回相同的结果。", example = "1xxxx104fb10e6ab3dde05c7d6aae7077a1cf8")
    private String requestId;

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定id", example = "1xxxx104fb10e6ab3dde05c7d6aae7077a1cf8")
    private String bindId;

    @ApiModelProperty(hidden = true)
    private String vccId;

}
