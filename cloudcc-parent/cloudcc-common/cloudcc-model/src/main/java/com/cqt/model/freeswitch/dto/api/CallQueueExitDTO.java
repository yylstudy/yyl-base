package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:18
 * 退出话务排队** *call_queue_exit*
 * <p>
 * 排队超时时调用，用于结束话务排队，接着走IVR流程
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallQueueExitDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 5794999058871110028L;

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 构建对象
     */
    public static CallQueueExitDTO build(String reqId, String companyCode, String uuid) {
        CallQueueExitDTO callQueueExitDTO = new CallQueueExitDTO();
        callQueueExitDTO.setCompanyCode(companyCode);
        callQueueExitDTO.setReqId(reqId);
        callQueueExitDTO.setUuid(uuid);
        return callQueueExitDTO;
    }
}
