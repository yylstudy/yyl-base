package com.cqt.model.freeswitch.dto.api;

import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.model.client.dto.ClientAnswerDTO;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.freeswitch.dto.event.CallInEventDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:18
 * 接听入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AnswerDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -4153726012208153888L;

    /**
     * 否  | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    @JsonProperty("video")
    private Integer video;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 否  | 视频分辨率【480， 720， 1280】 默认480
     */
    @JsonProperty("pixels")
    private Integer pixels;

    /**
     * 否  | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    private Integer audio;

    /**
     * 构建对象
     */
    public static AnswerDTO build(ClientAnswerDTO clientAnswerDTO) {
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setReqId(clientAnswerDTO.getReqId());
        answerDTO.setCompanyCode(clientAnswerDTO.getCompanyCode());
        answerDTO.setUuid(clientAnswerDTO.getUuid());
        answerDTO.setAudio(clientAnswerDTO.getAudio());
        answerDTO.setVideo(clientAnswerDTO.getVideo());
        answerDTO.setPixels(clientAnswerDTO.getPixels());
        return answerDTO;
    }

    /**
     * 构建对象
     */
    public static AnswerDTO build(CallInEventDTO callInEventDTO) {
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setReqId(callInEventDTO.getUuid());
        answerDTO.setCompanyCode(callInEventDTO.getCompanyCode());
        answerDTO.setUuid(callInEventDTO.getUuid());
        answerDTO.setAudio(MediaStreamEnum.valueOf(callInEventDTO.getData().getAudio()).getCode());
        answerDTO.setVideo(MediaStreamEnum.valueOf(callInEventDTO.getData().getVideo()).getCode());
        return answerDTO;
    }
}
