package com.cqt.model.unicom.dto;


import io.swagger.annotations.Api;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "联通集团总部(江苏)短信状态推送入参")
@Data
public class SmsStatusDTO {

    private String bindingId;

    @NotBlank(message = "callId不能为空")
    private String callId;

    private String phoneNumberA;

    @NotBlank(message = "phoneNumberX不能为空")
    private String phoneNumberX;

    private String phoneNumberB;

    private String phoneNumberC;

    @NotBlank(message = "receiveTime")
    @Pattern(regexp = "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]1|[0-9]1[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|1[0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$",
            message = "receiveTime格式: yyyy-MM-dd HH:mm:ss")
    private String receiveTime;

    @NotBlank(message = "state不能为空")
    private String state;

    private String smsContent;
}
