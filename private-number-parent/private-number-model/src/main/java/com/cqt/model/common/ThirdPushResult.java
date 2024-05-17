package com.cqt.model.common;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 美团通用返回体
 *
 * @author hlx
 * @date 2021-09-09
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThirdPushResult implements Serializable {

    private static final long serialVersionUID = -5173464241723713830L;
    /**
     * 状态码 必须
     */
    private String code;

    /**
     * 错误信息
     */
    private String message;

    public static ThirdPushResult fail(String code, String message) {
        ThirdPushResult result = new ThirdPushResult();
        result.code = code;
        result.message = message;
        return result;
    }

    public static ThirdPushResult ok(String code, String message) {
        ThirdPushResult result = new ThirdPushResult();
        result.code = code;
        result.message = message;
        return result;
    }

    public static ThirdPushResult ok() {
        ThirdPushResult result = new ThirdPushResult();
        result.code = "0000";
        result.message = "成功";
        return result;
    }

}
