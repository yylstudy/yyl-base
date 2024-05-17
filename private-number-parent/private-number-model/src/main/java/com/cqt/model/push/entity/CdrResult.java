package com.cqt.model.push.entity;

import lombok.Data;

/**
 * @author hlx
 * @date 2022-04-27
 */
@Data
public class CdrResult {

    /**
     * 返回码  0000 为成功
     */
    private String result;

    /**
     * 话单callId
     */
    private String callId;

    /**
     * 原因
     */
    private String reason;

    public static CdrResult fail(String reason) {
        CdrResult cdrResult = new CdrResult();
        cdrResult.setResult("99");
        cdrResult.setReason(reason);
        return cdrResult;
    }

    public static CdrResult ok(String callId) {
        CdrResult cdrResult = new CdrResult();
        cdrResult.setCallId(callId);
        cdrResult.setResult("0000");
        cdrResult.setReason("success");
        return cdrResult;
    }


    public static CdrResult ok() {
        CdrResult cdrResult = new CdrResult();
        cdrResult.setResult("0000");
        cdrResult.setReason("success");
        return cdrResult;
    }
}