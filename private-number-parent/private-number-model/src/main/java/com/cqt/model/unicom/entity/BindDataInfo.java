package com.cqt.model.unicom.entity;

import io.swagger.annotations.Api;
import lombok.Data;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags="号码绑定接口查询返回参数")
@Data
public class BindDataInfo {

    private String calledNum;

    private String vccId;

    private String displayNum;

    private String callNum;

    private String callerIvr;

    private String calledIvr;

    private String callerIvrBefore;

    private Integer enableRecord;

    private String numtype;

    private Integer type;

    private Integer maxDuration;

    private String extNum;

    private String bindId;

    private String areaCode;

    private String userData;

    private String recordFileFormat;

    private Integer recordMode;

    private Integer dualRecordMode;

    private String lastMinVoice;


}
