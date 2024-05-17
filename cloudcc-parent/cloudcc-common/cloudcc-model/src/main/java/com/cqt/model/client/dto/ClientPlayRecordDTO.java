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
 * SDK 播放录音参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientPlayRecordDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 坐席通话id
     */
    private String uuid;

    /**
     * 是 录音文件路径
     */
    @JsonProperty("file_path")
    @NotEmpty(message = "[file_path]不能为空!")
    private String filePath;

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

}
