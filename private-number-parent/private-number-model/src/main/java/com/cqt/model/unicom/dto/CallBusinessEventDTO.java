package com.cqt.model.unicom.dto;

import io.swagger.annotations.Api;
import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "联通集团总部(江苏)业务事件通话状态推送入参")
@Data
public class CallBusinessEventDTO {
    @NotBlank(message = "event不能为空")
    private String event;

    @NotBlank(message = "timestamp不能为空")
    private String timestamp;

    private String bindingId;

    private String callId;

    @NotBlank(message = "serviceCode不能为空")
    private String serviceCode;

    private String phoneNumberA;

    private String phoneNumberX;

    private String phoneNumberB;

    private String phoneNumberY;

    private String extensionNumber;

    private Object data;



}
