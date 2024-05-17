package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:35
 * 咨询、转接、三方通话、耳语、监听
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XferDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 2559641304076693956L;

    /**
     * 是  | 当前通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 否  | 转接、咨询的过程中播放的等待语音文件，不填走播放默认文件，带后缀
     */
    @JsonProperty("x_file_name")
    private String xFileName;

    /**
     * 是  | 被咨询、转接、三方通话、耳语、监听的通话ID
     */
    @JsonProperty("x_uuid")
    private String xUuid;

    /**
     * 是  | 【consult:咨询，trans:转接，three_way:三方通话，whisper:耳语，eavesdrop:监听】
     */
    @JsonProperty("xfer_action")
    private String xferAction;

}
