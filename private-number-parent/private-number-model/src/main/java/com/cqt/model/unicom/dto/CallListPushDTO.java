package com.cqt.model.unicom.dto;

import io.swagger.annotations.Api;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author zhengsuhao
 * @date 2022/12/6
 */
@Api(tags="联通集团总部(江苏)话单推送入参")
@Data
public class CallListPushDTO {

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
    @Pattern(regexp = "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]|[0-9][1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|1[0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$",
            message = "callTime格式: yyyy-MM-dd HH:mm:ss")
    private String callTime;

    @NotBlank(message = "ringingTime不能为空")
    @Pattern(regexp = "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]|[0-9][1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|1[0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$",
            message = "ringingTime格式: yyyy-MM-dd HH:mm:ss")
    private String ringingTime;

    @NotBlank(message = "startTime不能为空")
    @Pattern(regexp = "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]|[0-9][1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|1[0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$",
            message = "startTime格式: yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @NotBlank(message = "releaseTime不能为空")
    @Pattern(regexp = "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]|[0-9][1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|1[0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$",
            message = "releaseTime格式: yyyy-MM-dd HH:mm:ss")
    private String releaseTime;

    @NotBlank(message = "callId不能为空")
    private String callId;

    @NotBlank(message = "releaseDirection不能为空")
    private String releaseDirection;

    @NotBlank(message = "releaseCause不能为空")
    private String releaseCause;

    @NotBlank(message = "callRecording不能为空")
    private String callRecording;

    private String recordingUrl;

    private String recordingMode;

    private String transferPhoneNumber;

    private String transferReason;

    private String ability;

    private String callRecognitionResult;

    private String additionalData;



}
