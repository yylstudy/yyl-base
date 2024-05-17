package com.cqt.model.unicom.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags = "联通集团总部(江苏)号码绑定查询出参")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NumberBindingQueryVO {

    /**
     * 真实号码
     */
    @ApiModelProperty(value = "真实号码")
    private String phoneNumberA;
    /**
     * 小号号码
     */
    @ApiModelProperty(value = "小号号码")
    private String phoneNumberX;
    /**
     * 对端显示小号号码
     */
    @ApiModelProperty(value = "对端显示小号号码")
    private String phoneNumberY;
    /**
     * 对端号码
     */
    @ApiModelProperty(value = "对端号码")
    private String phoneNumberB;
    /**
     * 放音编码
     */
    @ApiModelProperty(value = "放音编码")
    private String audioCode;
    /**
     * 录音控制
     */
    @ApiModelProperty(value = "录音控制")
    private String callRecording;
    /**
     * 录音文件格式
     */
    @ApiModelProperty(value = "录音文件格式")
    private Integer callRecordingFileFormat;
    /**
     * 来显控制
     */
    @ApiModelProperty(value = "来显控制")
    private String callDisplay;
    /**
     * 短信后缀
     */
    @ApiModelProperty(value = "短信后缀")
    private String smsSuffix;
    /**
     * 主叫TransMedia格式
     */
    @ApiModelProperty(value = "主叫TransMedia格式")
    private TransMedia callerTransMedia;
    /**
     * 被叫TransMedia格式
     */
    @ApiModelProperty(value = "被叫TransMedia格式")
    private TransMedia calledTransMedia;

    /**
     * 录音模式
     */
    @ApiModelProperty(value = "录音模式")
    private String callRecordingMode;
    /**
     * 呼叫的方式
     */
    @ApiModelProperty(value = "呼叫的方式")
    private Integer callWay;
    /**
     * 顺序呼叫时超时时间
     */
    @ApiModelProperty(value = "顺序呼叫时超时时间")
    private Integer callTimeout;

    /**
     * 按键收号对象
     */
    @ApiModelProperty(value = "按键收号对象")
    private DgtsEventInfo dgtsEventInfo;

    /**
     * 附加的数据
     */
    @ApiModelProperty(value = "附加的数据")
    private String additionalData;

    public static NumberBindingQueryVO notBind(String telA, String telX, String notBindIvrCode) {
        NumberBindingQueryVO numberBindingQueryVO = new NumberBindingQueryVO();
        //插入主叫号码
        numberBindingQueryVO.setPhoneNumberA(telA);
        //插入小号号码
        numberBindingQueryVO.setPhoneNumberX(telX);
        numberBindingQueryVO.setAudioCode("0,0," + notBindIvrCode);
        return numberBindingQueryVO;
    }
}
