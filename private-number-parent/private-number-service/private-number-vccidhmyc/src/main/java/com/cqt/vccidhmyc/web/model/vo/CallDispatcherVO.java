package com.cqt.vccidhmyc.web.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:06
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallDispatcherVO {

    @JsonProperty("STATECODE")
    private String stateCode;

    @JsonProperty("REMARK")
    private String remark = "";

    private Integer callLimit;

    @JsonProperty("VCCID")
    private String vccId = "";

    @JsonProperty("callin_95_num")
    private Integer callIn95Num;

    @JsonProperty("CALLINFIX")
    private Integer callInFix;

    @JsonProperty("callin_400_num")
    private Integer callIn400Num;

    @JsonProperty("MIDDLEINNUM")
    private String middleNum = "";

    @JsonProperty("ALLISRECORD")
    private Integer allIsRecord;

    @JsonProperty("FINDBINDRELATIONURL")
    private String findBindRelationUrl = "";

    @JsonProperty("LUANAME")
    private String luaName = "";

    @JsonProperty("AUTOHANGUP")
    private Integer autoHangup;

    @JsonProperty("FAILSTATEASR")
    private Integer failStateAsr;

    @JsonProperty("AANSWERTYPE")
    private String answerType = "";

    public static CallDispatcherVO fail(String errorMsg, String calledNum) {
        CallDispatcherVO callDispatcherVO = new CallDispatcherVO();
        callDispatcherVO.setStateCode("9999");
        callDispatcherVO.setRemark(errorMsg);
        callDispatcherVO.setMiddleNum(calledNum);
        return callDispatcherVO;
    }

    public static CallDispatcherVO error(String stateCode, String errorMsg) {
        CallDispatcherVO callDispatcherVO = new CallDispatcherVO();
        callDispatcherVO.setStateCode(stateCode);
        callDispatcherVO.setRemark(errorMsg);
        return callDispatcherVO;
    }
}
