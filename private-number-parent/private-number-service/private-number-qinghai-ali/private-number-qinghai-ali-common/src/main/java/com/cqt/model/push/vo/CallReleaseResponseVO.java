package com.cqt.model.push.vo;

import lombok.Data;

/**
 * @author zhengsuhao
 * date:  2023-02-08
 */
@Data
public class CallReleaseResponseVO {

    private Boolean success;

    private String message;

    private String code;

    public CallReleaseResponseVO() {
    }

    public CallReleaseResponseVO(String message, String code,Boolean success) {
        this.message = message;
        this.code = code;
        this.success = success;
    }

    public static CallReleaseResponseVO ok(String message, String code)
    {
        CallReleaseResponseVO callReleaseResponseVO = new CallReleaseResponseVO();
        callReleaseResponseVO.success = true;
        callReleaseResponseVO.message = message;
        callReleaseResponseVO.code = code;
        return callReleaseResponseVO;

    }
    public static CallReleaseResponseVO fail(String message, String code)
    {
        CallReleaseResponseVO callReleaseResponseVO = new CallReleaseResponseVO();
        callReleaseResponseVO.success = false;
        callReleaseResponseVO.message = message;
        callReleaseResponseVO.code = code;
        return callReleaseResponseVO;

    }
    public static CallReleaseResponseVO fail()
    {
        CallReleaseResponseVO callReleaseResponseVO = new CallReleaseResponseVO();
        callReleaseResponseVO.success = false;
        callReleaseResponseVO.message = "阿里话单结束推送服务返回结果为空";
        callReleaseResponseVO.code = "fail";
        return callReleaseResponseVO;

    }

}
