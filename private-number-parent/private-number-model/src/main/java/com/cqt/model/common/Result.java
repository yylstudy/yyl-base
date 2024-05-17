package com.cqt.model.common;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Result implements Serializable {

    private static final long serialVersionUID = -5173464241723713830L;
    /**
     * 状态码 必须
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    @JsonProperty("is_repeat")
    private Boolean isRepeat;

    @JsonProperty("area_code")
    private String areaCode;

    private Object data;



    public static Result ok(String message, Object data) {
        Result result = new Result();
        result.message = message;
        result.code = 0;
        result.data = data;
        return result;
    }

    public static Result ok() {
        Result result = new Result();
        result.message = "success";
        result.code = 0;
        return result;
    }

    public static Result ok(Object data) {
        Result result = new Result();
        result.message = "成功";
        result.code = 0;
        result.data = data;
        return result;
    }

    public static Result okRepeat(Object data) {
        Result result = new Result();
        result.message = "成功";
        result.code = 0;
        result.data = data;
        result.isRepeat = true;
        return result;
    }

    public static Result okByAreaCode(String areaCode) {
        Result result = new Result();
        result.message = "成功";
        result.code = 0;
        result.areaCode = areaCode;
        return result;
    }

    public static Result fail(Integer code, String message) {
        Result result = new Result();
        result.code = code;
        result.message = message;
        return result;
    }

    public static Result fail() {
        Result result = new Result();
        result.code = 500;
        result.message = "failed";
        return result;
    }

    public static Result ok(Integer code, String message) {
        Result result = new Result();
        result.code = code;
        result.message = message;
        return result;
    }

}
