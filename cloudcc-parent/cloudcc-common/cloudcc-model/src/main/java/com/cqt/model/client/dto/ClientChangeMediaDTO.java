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
 * date:  2023-07-05 14:28
 * SDK 音视频切换请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientChangeMediaDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    @NotEmpty(message = "[uuid]不能为空!")
    private String uuid;

    /**
     * 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    @NotNull(message = "[audio]不能为空!")
    private Integer audio;

    /**
     * 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    @JsonProperty("video")
    @NotNull(message = "[video]不能为空!")
    private Integer video;

    /**
     * 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)
     */
    @JsonProperty("pixels")
    private String pixels;

    /**
     * 是否发起另外一路，默认为true
     */
    @JsonProperty("reneg_other")
    private Boolean renegOther;
}
