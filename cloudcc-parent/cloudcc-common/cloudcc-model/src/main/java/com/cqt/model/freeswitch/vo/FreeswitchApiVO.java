package com.cqt.model.freeswitch.vo;

import com.cqt.base.enums.FreeswitchResultCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = -6842057420803459739L;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("req_id")
    private String reqId;

    @JsonProperty("result")
    private Boolean result;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("server_id")
    private String serverId;

    @JsonProperty("ret_code")
    private Integer code;

    public static FreeswitchApiVO error(String msg) {
        return FreeswitchApiVO.builder()
                .msg(msg)
                .result(false)
                .code(-1)
                .build();
    }

    /**
     * 返回值
     */
    public static FreeswitchApiVO fail(String msg) {
        return FreeswitchApiVO.builder()
                .msg(msg)
                .result(false)
                .code(FreeswitchResultCode.API_REQUEST_ERROR.getCode())
                .build();
    }

    /**
     * 返回值
     */
    public static FreeswitchApiVO fail(String msg, FreeswitchResultCode resultCode) {
        return FreeswitchApiVO.builder()
                .msg(msg)
                .result(false)
                .code(resultCode.getCode())
                .build();
    }

    /**
     * 返回值
     */
    public static FreeswitchApiVO notFindUuid(String msg) {
        return FreeswitchApiVO.builder()
                .msg(msg)
                .result(true)
                .code(FreeswitchResultCode.API_ERROR_UUID.getCode())
                .build();
    }

    /**
     * 返回值
     */
    public static FreeswitchApiVO response(String reqId, String msg, String uuid, Boolean result) {
        return FreeswitchApiVO.builder()
                .reqId(reqId)
                .msg(msg)
                .uuid(uuid)
                .result(result)
                .build();
    }
}
