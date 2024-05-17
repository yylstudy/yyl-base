package com.cqt.model.bind.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @since 2021/9/17 18:07
 * 查询绑定关系结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindInfoVO implements Serializable {

    private static final long serialVersionUID = -7342015082298040939L;
    
    /**
     * 状态码 0 成功
     */
    @ApiModelProperty("状态码 0 成功, 其他均失败")
    private Integer code;

    /**
     * 查询结果
     */
    @ApiModelProperty("消息")
    private String message;

    /**
     * 真实被叫号码
     */
    @JsonProperty("called_num")
    @ApiModelProperty("真实被叫号码")
    private String calledNum;

    /**
     * 被叫显示号码
     */
    @JsonProperty("display_num")
    @ApiModelProperty("被叫显示号码")
    private String displayNum;

    /**
     * 中间 发起呼叫的号码
     */
    @JsonProperty("call_num")
    @ApiModelProperty("中间X 发起呼叫的号码")
    private String callNum;

    /**
     * 主叫放音文件名
     */
    @JsonProperty("caller_ivr")
    @ApiModelProperty("主叫放音文件名")
    private String callerIvr;

    /**
     * 被叫放音文件名
     */
    @JsonProperty("called_ivr")
    @ApiModelProperty("被叫放音文件名")
    private String calledIvr;

    /**
     * 主叫呼通前放音
     */
    @JsonProperty("caller_ivr_before")
    @ApiModelProperty("主叫呼通前放音")
    private String callerIvrBefore;

    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要
     */
    @JsonProperty("enable_record")
    @ApiModelProperty("当前绑定关系是否需要录音 0：不需要 1：需要")
    private Integer enableRecord;

    /**
     * 号码类型 axb, axe，ayb
     */
    @JsonProperty("num_type")
    @ApiModelProperty("号码类型 axb, axe，ayb")
    private String numType;

    /**
     * 0：正常(系统默认0)
     * 1：禁用短信
     */
    @ApiModelProperty("是否禁用短信, 0 正常, 1 禁用")
    private Integer type;

    /**
     * 最大通话时长，单位秒
     */
    @JsonProperty("max_duration")
    @ApiModelProperty("最大通话时长，单位秒")
    private Integer maxDuration;

    /**
     * 分机号
     */
    @JsonProperty("ext_num")
    @ApiModelProperty("分机号")
    private String extNum;

    /**
     * 绑定Id
     */
    @JsonProperty("bind_id")
    @ApiModelProperty("绑定Id")
    private String bindId;

    @JsonProperty("area_code")
    @ApiModelProperty("地市编码")
    private String areaCode;

    @JsonProperty("user_data")
    @ApiModelProperty("用户透传字段")
    private String userData;

    /**
     * 绑定关系透传字段
     */
    @JsonProperty("transfer_data")
    @ApiModelProperty("绑定关系透传字段(绑定关系在本平台使用)")
    private Object transferData;

    /**
     * 指示呼叫转接的录音格式，仅下列值有效。默认是 wav。
     * mp3
     * wav
     */
    @JsonProperty("record_file_format")
    @ApiModelProperty("录音格式; wav, mp3")
    private String recordFileFormat = "wav";

    /**
     * 录音方式。
     * 0：混音，即通话双方的声音混合在一个声道中。
     * 1：双声道，即通话双方的声音分别录制在左、右两个声道中。
     * 如果不携带该参数，参数值默认为0。
     */
    @JsonProperty("record_mode")
    @ApiModelProperty("录音方式: 0：混音，即通话双方的声音混合在一个声道中;  1：双声道，即通话双方的声音分别录制在左、右两个声道中。")
    private Integer recordMode = 0;

    /**
     * 双声道录音模式，取值范围如下：
     * 0：主叫录音到左声道，被叫录音到右声道。
     * 1：被叫录音到左声道，主叫录音到右声道。
     * 录音模式为双声道时有效，而且是必选
     * 默认主叫录音到左声道，被叫录音到右声道
     */
    @JsonProperty("dual_record_mode")
    @ApiModelProperty("双声道录音模式: 0：主叫录音到左声道，被叫录音到右声道。, 1：被叫录音到左声道，主叫录音到右声道。")
    private Integer dualRecordMode = 0;

    /**
     * 通话最后一分钟放音
     */
    @JsonProperty("last_min_voice")
    @ApiModelProperty("通话最后一分钟放音")
    private String lastMinVoice;

    @ApiModelProperty("企业id")
    private String vccId;

    /**
     * 话务控制操作类型：
     * REJECT：（拦截）
     * CONTINUE：（接续）
     * IVR：IVR（收取用户输入内容）
     */
    @JsonProperty("control_operate")
    private String controlOperate;

    public BindInfoVO(Integer code, String message, String callerIvr, String numType) {
        this.code = code;
        this.message = message;
        this.callerIvr = callerIvr;
        this.numType = numType;
    }

    public BindInfoVO(Integer code, String message, String callerIvr) {
        this.code = code;
        this.message = message;
        this.callerIvr = callerIvr;
    }
}
