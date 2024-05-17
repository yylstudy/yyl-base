package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 接听 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientAnswerDTO extends ClientRequestBaseDTO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 呼入uuid
     */
    @NotEmpty(message = "[uuid]不能为空!")
    private String uuid;

    /**
     * 否  | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    @JsonProperty("video")
    @NotNull(message = "[video]不能为空!")
    private Integer video;

    /**
     * 否  | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    @NotNull(message = "[audio]不能为空!")
    private Integer audio;

    /**
     * 否  | 视频分辨率【480， 720， 1280】 默认480
     */
    @JsonProperty("pixels")
    private Integer pixels;

    @JsonProperty("callin_channel")
    private String callinChannel;

}
