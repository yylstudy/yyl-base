package com.cqt.model.freeswitch.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-08-01 17:18
 * 企业当前并发数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompanyConcurrencyVO extends FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = -9035203034426164556L;

    /**
     * 音频并发
     */
    @JsonProperty("audio_concurrency")
    private Integer audioConcurrency;

    /**
     * 视频并发
     */
    @JsonProperty("video_concurrency")
    private Integer videoConcurrency;

    /**
     * 总并发
     */
    @JsonProperty("sum_concurrency")
    private Integer sumConcurrency;
}
