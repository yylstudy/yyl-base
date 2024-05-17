package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:22
 * 停止放音入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StopPlayDTO extends FreeswitchApiBase implements Serializable {

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 构建结束放音对象
     *
     * @param companyCode 企业id
     * @param uuid        通话id
     * @return 结束放音对象
     */
    public static StopPlayDTO build(String companyCode, String uuid) {
        StopPlayDTO stopPlayDTO = new StopPlayDTO();
        stopPlayDTO.setReqId(IdUtil.fastUUID());
        stopPlayDTO.setCompanyCode(companyCode);
        stopPlayDTO.setUuid(uuid);
        return stopPlayDTO;
    }
}
