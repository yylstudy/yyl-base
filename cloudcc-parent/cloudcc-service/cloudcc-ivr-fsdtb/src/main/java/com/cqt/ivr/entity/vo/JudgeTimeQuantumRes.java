package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 预约是否上班时间判断响应实体类
 */
@Data
@ApiModel("预约是否上班时间判断响应参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeTimeQuantumRes implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "响应结果", example = "yes")
    private String result;

    @ApiModelProperty(value = "响应码", example = "0")
    private String code;

    @ApiModelProperty(value = "工作信息  1:工作时间     2：非工作时间")
    private String workStatus;

    @ApiModelProperty(value = "消息")
    private String message;

    public static JudgeTimeQuantumRes OK(String message,String workStatus) {
        JudgeTimeQuantumRes judgeTimeQuantumRes = new JudgeTimeQuantumRes();
        judgeTimeQuantumRes.setResult("yes");
        judgeTimeQuantumRes.setCode("0");
        judgeTimeQuantumRes.setWorkStatus(workStatus);
        judgeTimeQuantumRes.setMessage(message);
        return judgeTimeQuantumRes;
    }

    public static JudgeTimeQuantumRes error(String message) {
        JudgeTimeQuantumRes judgeTimeQuantumRes = new JudgeTimeQuantumRes();
        judgeTimeQuantumRes.setResult("yes");
        judgeTimeQuantumRes.setCode("0");
        judgeTimeQuantumRes.setWorkStatus(null);
        judgeTimeQuantumRes.setMessage(message);
        return judgeTimeQuantumRes;
    }
}