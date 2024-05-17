package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:29
 * 通话保持、恢复接口
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HoldDTO  extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -7105646003670072865L;

    /**
     * 否  | 播放保持音文件，默认播放系统默认文件，带后缀
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 是  |【ture:保持通话，false:恢复通话】
     */
    @JsonProperty("value")
    private Boolean value;
}
