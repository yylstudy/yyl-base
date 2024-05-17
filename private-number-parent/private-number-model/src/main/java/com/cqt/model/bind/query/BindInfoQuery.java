package com.cqt.model.bind.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2021/9/10 18:04
 * 查询绑定关系参数
 */
@Data
public class BindInfoQuery {

    @ApiModelProperty("主叫号码")
    private String caller;

    @ApiModelProperty("被叫号码")
    private String called;

    @ApiModelProperty("呼叫Id")
    private String callId;

    @ApiModelProperty("收号结果（即用户输入的数字）")
    private String digitInfo;

    @ApiModelProperty("企业id")
    private String vccId;

    @ApiModelProperty("行业短信不生成ayb 1 是, 0 不是")
    private Integer industrySms = 0;

    @ApiModelProperty("短信场景 1 是, 0 不是")
    private Integer smsFlag = 0;

}
