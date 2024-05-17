package com.cqt.model.bind.axbn.query;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2022/4/13 9:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxbnBindInfoQuery extends BaseAuth implements Serializable {

    private static final long serialVersionUID = -1945634787363581605L;

    /**
     * 企业每个请求Id唯一，如果是同一个请求重复提交，则Id保持相同
     */
    @JsonProperty("request_id")
    @ApiModelProperty(value = "请求ID,request_id相同的请求，须返回相同的结果。")
    private String requestId;

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定id。")
    private String bindId;

    @JsonProperty("vcc_id")
    @ApiModelProperty(value = "企业id", hidden = true)
    private String vccId;

}
