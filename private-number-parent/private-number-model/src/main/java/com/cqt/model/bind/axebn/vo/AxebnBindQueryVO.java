package com.cqt.model.bind.axebn.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/10 12:55
 * AXEBN模式 响应参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxebnBindQueryVO implements Serializable {

    private static final long serialVersionUID = -4815811557965500655L;

    /**
     * 绑定关系ID
     */
    @JsonProperty("bind_id")
    private String bindId;

    @JsonProperty("request_id")
    private String requestId;

    /**
     * 虚拟号码
     */
    @JsonProperty("tel_x")
    private String telX;

    /**
     * 分机号
     */
    @JsonProperty("tel_x_ext")
    private String telXExt;

    @JsonProperty("tel_b")
    private String telB;

    @JsonProperty("tel_a")
    private String telA;

    @JsonProperty("area_code")
    private String areaCode;

    @JsonProperty("expiration")
    private Long expiration;

}
