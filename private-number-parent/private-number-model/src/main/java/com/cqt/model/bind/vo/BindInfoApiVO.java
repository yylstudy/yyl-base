package com.cqt.model.bind.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @since 2021/9/17 18:07
 * 对外接口 查询绑定关系结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindInfoApiVO implements Serializable {

    private static final long serialVersionUID = -8347621862064708854L;
    /**
     * 真实被叫号码
     */
    @JsonProperty("called_num")
    private String calledNum;

    /**
     * 被叫显示号码
     */
    @JsonProperty("display_num")
    private String displayNum;

    /**
     * 中间 发起呼叫的号码
     */
    @JsonProperty("call_num")
    private String callNum;

    /**
     * 主叫放音文件名
     */
    @JsonProperty("caller_ivr")
    private String callerIvr;

    /**
     * 被叫放音文件名
     */
    @JsonProperty("called_ivr")
    private String calledIvr;

    /**
     * 主叫呼通前放音
     */
    @JsonProperty("caller_ivr_before")
    private String callerIvrBefore;

    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要（数值字符串）
     */
    @JsonProperty("enable_record")
    private Integer enableRecord;

    /**
     * 号码类型 axb, axe，ayb
     */
    @JsonProperty("num_type")
    private String numType;

    /**
     * 话务控制操作类型：
     * REJECT：（拦截）
     * CONTINUE：（接续）
     * IVR：IVR（收取用户输入内容）
     */
    @JsonProperty("control_operate")
    private String controlOperate;

    /**
     * 0：正常(系统默认0)
     * 1：禁用短信
     */
    private Integer type;

    /**
     * 最大通话时长，单位秒
     */
    @JsonProperty("max_duration")
    private Integer maxDuration;

    /**
     * 分机号
     */
    @JsonProperty("ext_num")
    private String extNum;

    /**
     * 绑定Id
     */
    @JsonProperty("bind_id")
    private String bindId;

    @JsonProperty("area_code")
    private String areaCode;

    @JsonProperty("user_data")
    private String userData;

    /**
     * 指示呼叫转接的录音格式，仅下列值有效。默认是 wav。
     * mp3
     * wav
     */
    @JsonProperty("record_file_format")
    private String recordFileFormat = "wav";

    /**
     * 录音方式。
     * 0：混音，即通话双方的声音混合在一个声道中。
     * 1：双声道，即通话双方的声音分别录制在左、右两个声道中。
     * 如果不携带该参数，参数值默认为0。
     */
    @JsonProperty("record_mode")
    private Integer recordMode = 0;

    /**
     * 双声道录音模式，取值范围如下：
     * 0：主叫录音到左声道，被叫录音到右声道。
     * 1：被叫录音到左声道，主叫录音到右声道。
     * 录音模式为双声道时有效，而且是必选
     * 默认主叫录音到左声道，被叫录音到右声道
     */
    @JsonProperty("dual_record_mode")
    private Integer dualRecordMode = 0;

    /**
     * 通话最后一分钟放音
     */
    @JsonProperty("last_min_voice")
    private String lastMinVoice;

    private String vccId;

    public static BindInfoApiVO fail(String controlOperate, String callerIvr) {
        return BindInfoApiVO.builder()
                .controlOperate(controlOperate)
                .callerIvr(callerIvr)
                .build();
    }
}
