package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:32
 * 录制通话接口入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecordDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -7429956347291170124L;

    /**
     * 否  | 是否立体声录制，默认否(单声道)
     */
    @JsonProperty("is_stereo")
    private Long isStereo;

    /**
     * 否  | 是否双向录制， 默认单向
     */
    @JsonProperty("record_contact")
    private Boolean recordContact;

    /**
     * 否  | 录制文件后缀【mp3, wav, mp4】默认mp3
     */
    @JsonProperty("record_suffix")
    private String recordSuffix;

    /**
     * 指定录制文件名, 不带后缀
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 构建对象
     *
     * @return RecordDTO
     */
    public static RecordDTO build(String companyCode, String uuid) {
        RecordDTO recordDTO = new RecordDTO();
        recordDTO.setReqId(IdUtil.fastUUID());
        recordDTO.setCompanyCode(companyCode);
        recordDTO.setUuid(uuid);
        return recordDTO;
    }
}
