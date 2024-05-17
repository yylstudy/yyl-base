package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 播放录音参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientPlayRecordControlDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 是  | 操作类型【pause:暂停, resume:恢复, replay:重播, seek:指定时间, speed:倍速播放, stop:停止播放, volume:音量】
     */
    @NotEmpty(message = "[action]不能为空")
    private String action;

    /**
     * 播放速度值，负值慢速，正值快速
     */
    @JsonProperty("speed_value")
    private Integer speedValue;

    /**
     * 播放音量，负值降音，正值升音
     */
    @JsonProperty("volume_value")
    private Integer volumeValue;

    /**
     * 指定位置(毫秒)
     */
    @JsonProperty("seek_value")
    private Integer seekValue;

}
