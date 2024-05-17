package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:02
 * 挂断入参
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class HangupDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 2868602070415618256L;

    /**
     * 是  | 通话ID
     */
    private String uuid;

    @JsonProperty("ext_id")
    private String extId;

    /**
     * 否  | 是否挂断另外一路，默认为true
     */
    @JsonProperty("hangup_other")
    private Boolean hangupOther = false;

    /**
     * 挂断原因
     */
    @JsonProperty("hangup_cause")
    private String hangupCause;

    /**
     * 复位 转化对象
     */
    public static HangupDTO buildReset(String companyCode, String extId, HangupCauseEnum hangupCause) {
        HangupDTO hangupDTO = new HangupDTO();
        hangupDTO.setReqId(IdUtil.fastUUID());
        hangupDTO.setCompanyCode(companyCode);
        hangupDTO.setExtId(extId);
        hangupDTO.setHangupOther(true);
        hangupDTO.setHangupCause(hangupCause.getDesc());
        return hangupDTO;
    }

    /**
     * 转化对象
     */
    public static HangupDTO build(String companyCode, Boolean hangupOther, String uuid, HangupCauseEnum hangupCause) {
        HangupDTO hangupDTO = new HangupDTO();
        hangupDTO.setReqId(IdUtil.fastUUID());
        hangupDTO.setCompanyCode(companyCode);
        hangupDTO.setUuid(uuid);
        hangupDTO.setHangupOther(hangupOther);
        hangupDTO.setHangupCause(hangupCause.getDesc());
        return hangupDTO;
    }

    /**
     * 转化对象
     */
    public static HangupDTO build(String companyCode, String uuid, HangupCauseEnum hangupCause) {
        HangupDTO hangupDTO = new HangupDTO();
        hangupDTO.setReqId(IdUtil.fastUUID());
        hangupDTO.setCompanyCode(companyCode);
        hangupDTO.setUuid(uuid);
        hangupDTO.setHangupOther(false);
        hangupDTO.setHangupCause(hangupCause.getDesc());
        return hangupDTO;
    }
}
