package com.cqt.model.bind.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * @since 2021/9/10 18:04
 * 对外接口 查询绑定关系参数
 */
@Data
public class BindInfoApiQuery {

    @ApiModelProperty("主叫号码")
    private String caller;

    @ApiModelProperty("被叫号码")
    private String called;

    @ApiModelProperty("呼叫Id")
    private String callId;

    @ApiModelProperty("收号结果（即用户输入的数字）")
    private String digitInfo;

    @ApiModelProperty(value = "企业id", hidden = true)
    private String vccId;

    @ApiModelProperty("行为类型,CALL:呼叫行为,SMS:短信行为")
    private String behaviorType;
}
