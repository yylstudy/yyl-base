package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:34
 * 结束录音入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StopRecordDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 3817055275725464791L;

    /**
     * 录制ID，结束录制时候带过来
     */
    @JsonProperty("record_id")
    private String recordId;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    @Builder
    public StopRecordDTO(String reqId, String serviceCode, String serverId, String companyCode, String recordId, String uuid) {
        super(reqId, serviceCode, serverId, companyCode);
        this.recordId = recordId;
        this.uuid = uuid;
    }

    /**
     * 构建对象
     */
    public static StopRecordDTO build(String companyCode, String recordId, String uuid) {
        return StopRecordDTO.builder()
                .reqId(IdUtil.fastUUID())
                .companyCode(companyCode)
                .uuid(uuid)
                .recordId(recordId)
                .build();
    }
}
