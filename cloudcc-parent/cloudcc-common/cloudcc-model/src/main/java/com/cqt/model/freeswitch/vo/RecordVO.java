package com.cqt.model.freeswitch.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-08-01 17:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecordVO extends FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = -9035203034426164556L;

    /**
     * 录制ID，结束录制时候带过来
     */
    @JsonProperty("record_id")
    private String recordId;

    /**
     * 录制文件名
     */
    @JsonProperty("lcc_record_file")
    private String recordFileName;
}
