package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:20
 * 放音入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlaybackDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -1804528466643980117L;

    /**
     *  否  | 播放次数，默认一次；0表示循环播放
     */
    @JsonProperty("count")
    private Integer count;

    /**
     * 否  | 使用tts引擎，默认系统指定
     */
    @JsonProperty("engine")
    private String engine;

    /**
     * 是  | 播放文件名，播放文件流程时必填，需要带后缀
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 否  | 播放内容，走tts流程时必填，如果为空走播放文件流程
     */
    @JsonProperty("text")
    private String text;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 否  | 使用tts引擎voice，走tts流程时必填
     */
    @JsonProperty("voice")
    private String voice;

    /**
     * 呼入ivr播放排队等待音
     *
     * @param callInIvrActionDTO 呼入ivr参数
     * @return 放音实体
     */
    public static PlaybackDTO buildWaitPlayback(CallInIvrActionDTO callInIvrActionDTO) {
        PlaybackDTO playbackDTO = new PlaybackDTO();
        playbackDTO.setReqId(IdUtil.fastUUID());
        playbackDTO.setCompanyCode(callInIvrActionDTO.getCompanyCode());
        playbackDTO.setUuid(callInIvrActionDTO.getUuid());
        playbackDTO.setFileName(callInIvrActionDTO.getSkillWaitTone());
        playbackDTO.setCount(0);
        return playbackDTO;
    }

    public static PlaybackDTO buildWaitPlayback(UserQueueUpDTO userQueueUpDTO) {
        PlaybackDTO playbackDTO = new PlaybackDTO();
        playbackDTO.setReqId(IdUtil.fastUUID());
        playbackDTO.setCompanyCode(userQueueUpDTO.getCompanyCode());
        playbackDTO.setUuid(userQueueUpDTO.getUuid());
        playbackDTO.setFileName(userQueueUpDTO.getWaitTone());
        playbackDTO.setCount(0);
        return playbackDTO;
    }

    /**
     * 放音对象
     */
    public static PlaybackDTO build(String companyCode, String uuid, String fileName, Integer count) {
        PlaybackDTO playbackDTO = new PlaybackDTO();
        playbackDTO.setReqId(IdUtil.fastUUID());
        playbackDTO.setCompanyCode(companyCode);
        playbackDTO.setUuid(uuid);
        playbackDTO.setFileName(fileName);
        playbackDTO.setCount(count);
        return playbackDTO;
    }
}
