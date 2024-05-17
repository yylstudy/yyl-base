package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:23
 * 放音收号
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlayAndGetDigitsDTO  extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 854271563438342758L;

    /**
     * 收键正则，默认为空
     */
    @JsonProperty("digits_regex")
    private String digitsRegex;

    /**
     * 使用tts引擎，默认系统指定
     */
    @JsonProperty("engine")
    private String engine;

    /**
     * 播放文件名，播放文件流程时必填，带后缀
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 错误播放文件名，播放文件流程时必填，带后缀
     */
    @JsonProperty("invalid_file_name")
    private String invalidFileName;

    /**
     * 错误播放内容，走tts流程时必填，如果为空走播放文件流程
     */
    @JsonProperty("invalid_text")
    private String invalidText;

    /**
     * 文件播放次数，默认1
     */
    @JsonProperty("loop")
    private Integer loop;

    /**
     * 最大按键数，默认1
     */
    @JsonProperty("max")
    private Integer max;

    /**
     * 最小按键数，默认1
     */
    @JsonProperty("min")
    private Integer min;

    /**
     * 错误重试次数，默认1
     */
    @JsonProperty("retry_times")
    private Integer retryTimes;

    /**
     * 结束按键，默认#
     */
    @JsonProperty("terminators")
    private String terminators;

    /**
     * 播放内容，走tts流程时必填，如果为空走播放文件流程
     */
    @JsonProperty("text")
    private String text;

    /**
     * 收号超时(ms)，默认5000
     */
    @JsonProperty("timeout")
    private Integer timeout;

    /**
     * 收键超时(ms)，默认5000
     */
    @JsonProperty("digit_timeout")
    private Integer digitTimeout;

    /**
     * string 是 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 使用tts引擎voice，走tts流程时必填
     */
    @JsonProperty("voice")
    private String voice;

    /**
     * 留言放音收号
     *
     * @param callInIvrActionDTO 呼入留言参数
     * @return 放音收号对象
     */
    public static PlayAndGetDigitsDTO messagePlay(CallInIvrActionDTO callInIvrActionDTO) {
        PlayAndGetDigitsDTO digitsDTO = new PlayAndGetDigitsDTO();
        digitsDTO.setReqId(IdUtil.fastUUID());
        digitsDTO.setCompanyCode(callInIvrActionDTO.getCompanyCode());
        digitsDTO.setUuid(callInIvrActionDTO.getUuid());
        digitsDTO.setMin(1);
        digitsDTO.setMax(1);
        digitsDTO.setRetryTimes(3);
        digitsDTO.setLoop(1);
        digitsDTO.setFileName(callInIvrActionDTO.getMessageStartRecordTone());
        int timeout = callInIvrActionDTO.getTimeout() * 1000;
        digitsDTO.setDigitTimeout(timeout);
        digitsDTO.setTimeout(timeout);
        digitsDTO.setInvalidText(StrFormatter.format("请按{}号键结束留言", callInIvrActionDTO.getDtmf()));
        return digitsDTO;
    }

}
