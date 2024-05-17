package com.cqt.model.unicom.dto;


import io.swagger.annotations.Api;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "联通集团总部(江苏)业务开始推送入参")
@Data
public class CallConnectionStatusDTO {

    private String phoneNumberA;

    @NotBlank(message = "phoneNumberX不能为空")
    private String phoneNumberX;

    private String phoneNumberB;

    private String phoneNumberC;

    private String phoneNumberY;

    private String dgts;

    private String bindingId;

    @NotBlank(message = "callType不能为空")
    private String callType;

    @NotBlank(message = "callResult不能为空")
    private String callResult;

    @NotBlank(message = "callTime不能为空")
    private String callTime;

    @NotBlank(message = "callId不能为空")
    private String callId;

    @NotBlank(message = "callRecording不能为空")
    private String callRecording;

    private String smsContent;

    private String additionalData;

}
