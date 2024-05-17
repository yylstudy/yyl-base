package com.cqt.model.dailtest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 拨测
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DialTestDTO implements Serializable {

    private static final long serialVersionUID = -4215105003093436863L;

    @NotBlank(message = "appkey 不能为空")
    private String appkey;

    @NotBlank(message = "ts 不能为空")
    private String ts;

    @NotBlank(message = "sign 不能为空")
    private String sign;

    /**
     * 请求ID。用于唯一标识本次拨测请求，需要通过拨测话单透传给美团。
     */
    @JsonProperty("request_id")
    @ApiModelProperty(value = "请求ID。用于唯一标识本次拨测请求，需要通过拨测话单透传给美团。")
    @NotBlank(message = "request_id 不能为空")
    private String requestId;

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定ID。原始AXB的绑定ID。")
    @NotBlank(message = "bindId 不能为空")
    private String bindId;

    /**
     * 号码A, 主叫真实号码，例如：骑手手机号
     */
    @JsonProperty("phone_calling")
    @ApiModelProperty(value = "号码A, 主叫真实号码，例如：骑手手机号")
    @NotBlank(message = "phone_calling 不能为空")
    private String phoneCalling;

    /**
     * 号码B, 被叫号码，骑手通过X号码呼叫的真实被叫。
     */
    @JsonProperty("phone_called")
    @ApiModelProperty(value = "号码B, 被叫号码，骑手通过X号码呼叫的真实被叫。")
    @NotBlank(message = "phone_called 不能为空")
    private String phoneCalled;

    /**
     * 虚拟号码X, 虚拟号，骑手和真实被叫绑定的号码
     * axb: 虚拟号x
     * ax: 虚拟号x,不带分机号。
     */
    @JsonProperty("phone_x")
    @ApiModelProperty(value = "虚拟号，骑手和真实被叫绑定的号码")
    @NotBlank(message = "phone_x 不能为空")
    private String phoneX;

    /**
     * ax号码的分机号
     */
    @JsonProperty("phone_x_ext")
    @ApiModelProperty(value = " ax号码的分机号")
    private String phoneXext;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    @JsonProperty("area_code")
    @ApiModelProperty(value = " 以0开头的虚拟号区号（如010）")
    @NotBlank(message = "area_code 不能为空")
    private String areaCode;

    private String vccId;

}
