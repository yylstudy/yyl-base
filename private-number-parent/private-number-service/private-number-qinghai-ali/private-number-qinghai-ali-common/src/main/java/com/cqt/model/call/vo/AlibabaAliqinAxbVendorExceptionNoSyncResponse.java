package com.cqt.model.call.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

/**
 * 淘宝异常号码状态同步接口响应值
 *
 * @author Xienx
 * @date 2023年02月06日 11:07
 */
@Data
public class AlibabaAliqinAxbVendorExceptionNoSyncResponse implements Serializable {

    private static final long serialVersionUID = 1713947003302984261L;

    @JsonAlias("result")
    private Response result;

    @Data
    public static class Response implements Serializable {

        private static final long serialVersionUID = 8261892218185251677L;

        @JsonAlias("code")
        private String code;

        @JsonAlias("message")
        private String message;

        @JsonAlias("module")
        private Boolean module;
    }
}
