package com.cqt.model.company.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-07 15:46
 * 企业信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@TableName("cloudcc_company_info")
public class CompanyInfo implements Serializable {

    private static final long serialVersionUID = -4992793045775939222L;

    /**
     * 企业编码
     */
    @TableId(type = IdType.INPUT)
    private String companyCode;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 计费企业标识
     */
    private String vccId;

    /**
     * 渠道Id
     */
    private String channelId;

    /**
     * 企业主显号
     */
    private String mainDisplayNumber;

    /**
     * 企业状态 0：禁用，1：启用
     */
    private Integer state;

    /**
     * 坐席绑定分机模式 1 自动绑定 2 自定义绑定
     */
    private Integer extBindMode;

    /**
     * 分机注册方式 1 webrtc 2 其他第三方话机
     */
    private Integer extRegMode;

    /**
     * 自动示忙 0 关闭 1 开启
     * 话务分配给坐席，坐席设置自动应答且应答失败，或坐席设置手动应答且拒接来电，坐席状态是否自动变更为示忙
     */
    private Integer autoShowBusy;

    /**
     * 坐席账号是否支持互顶 0：否 1：是
     */
    private Integer agentAccountKick;

    /**
     * 是否并发管控 1-是 , 0-否
     */
    @TableField(value = "is_control")
    @JsonProperty("isControl")
    private Integer isControl;

    /**
     * 音频并发
     */
    @TableField(value = "audio_concurrency")
    private Integer audioConcurrency;

    /**
     * 视频并发
     */
    @TableField(value = "video_concurrency")
    private Integer videoConcurrency;

    /**
     * 语音录制格式（1：mp3；2：wav）
     */
    private String voiceRecordFormat;

    /**
     * 视频录制格式（1：mp4，2：mp3）
     */
    private String videoRecordFormat;

    /**
     * 录制声道（1：单声道，2：双声道）
     */
    private Integer recordChannel;

    /**
     * 坐席呼出对端振铃超时时间
     * 可设置坐席外呼后最长的振铃时间，默认为30秒，最大30秒；
     *
     * @since 7.0.0
     */
    @TableField(value = "call_out_ring_timeout")
    @JsonProperty(value = "callOutRingTimeout")
    private Integer callOutRingTimeout;

    /**
     * 坐席接收来电振铃超时时间
     * 可设置话务分配坐席后，坐席最长的振铃时间，默认为20秒，最大30秒；
     *
     * @since 7.0.0
     */
    @TableField(value = "call_in_ring_timeout")
    @JsonProperty(value = "callInRingTimeout")
    private Integer callInRingTimeout;

    /**
     * 坐席接收来电应答的提示音
     * 客户呼入-转坐席, 坐席接通,给坐席放音
     *
     * @since 7.0.0
     */
    @TableField("call_in_agent_answer_playback_switch")
    private Integer callInAgentAnswerPlaybackSwitch;

    /**
     * 坐席呼出对端应答后的提示音
     * 坐席呼出-客户接通, 给坐席放音
     *
     * @since 7.0.0
     */
    @TableField("call_out_peer_answer_playback_switch")
    private Integer callOutPeerAnswerPlaybackSwitch;

    /**
     * 呼出-A路录制节点
     *
     * @see com.cqt.base.enums.cdr.CalloutOwnRecorderEnum
     * @since 7.0.0
     */
    @TableField("call_out_recording_options_a")
    @JsonProperty("callOutRecordingOptionsA")
    private Integer callOutRecordNodeA;

    /**
     * 呼出-B路录制节点
     *
     * @see com.cqt.base.enums.cdr.CalloutPeerRecorderEnum
     * @since 7.0.0
     */
    @TableField("call_out_recording_options_b")
    @JsonProperty("callOutRecordingOptionsB")
    private Integer callOutRecordNodeB;

    /**
     * 呼入-A路录制节点
     *
     * @see com.cqt.base.enums.cdr.CallInOwnRecorderEnum
     * @since 7.0.0
     */
    @TableField("call_in_recording_options_a")
    @JsonProperty("callInRecordingOptionsA")
    private Integer callInRecordNodeA;

    /**
     * 呼入-B路录制节点
     *
     * @see com.cqt.base.enums.cdr.CallInPeerRecorderEnum
     * @since 7.0.0
     */
    @TableField("call_in_recording_options_b")
    @JsonProperty("callInRecordingOptionsB")
    private Integer callInRecordNodeB;

    /**
     * 满意度录制（0：关闭 1：开启）
     *
     * @since 7.0.0
     */
    private Integer satisfactionRecord;

}
