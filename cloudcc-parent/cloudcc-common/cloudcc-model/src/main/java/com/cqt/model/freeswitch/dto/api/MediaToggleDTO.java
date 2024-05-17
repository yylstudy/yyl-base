package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:06
 * 通话切换
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MediaToggleDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -8654037742556340221L;

    /**
     * 是  | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】
     */
    @JsonProperty("audio")
    private Integer audio;

    /**
     * 否  | 视频分辨率【480， 720， 1280】 默认480
     */
    @JsonProperty("pixels")
    private String pixels;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 是  | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】
     */
    @JsonProperty("video")
    private Integer video;

}
