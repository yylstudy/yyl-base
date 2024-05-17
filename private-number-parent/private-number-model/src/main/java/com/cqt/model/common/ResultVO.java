package com.cqt.model.common;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用返回体
 *
 * @author hlx
 * @date 2021-09-09
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = -6604682255747494731L;

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

    private T data;

    public static <T> ResultVO<T> ok(String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.message = message;
        result.code = 0;
        result.data = data;
        return result;
    }

    public static <T> ResultVO<T> ok() {
        ResultVO<T> result = new ResultVO<>();
        result.message = "成功";
        result.code = 0;
        return result;
    }

    public static <T> ResultVO<T> ok(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.message = "成功";
        result.code = 0;
        result.data = data;
        return result;
    }

    public static <T> ResultVO<T> okRepeat(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.message = "成功";
        result.code = 0;
        result.data = data;
        result.isRepeat = true;
        return result;
    }

    public static <T> ResultVO<T> okByAreaCode(String areaCode) {
        ResultVO<T> result = new ResultVO<>();
        result.message = "成功";
        result.code = 0;
        result.areaCode = areaCode;
        return result;
    }

    public static <T> ResultVO<T> fail(Integer code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> ResultVO<T> fail(Integer code, String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.code = code;
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> ResultVO<T> fail() {
        ResultVO<T> result = new ResultVO<>();
        result.code = 500;
        result.message = "failed";
        return result;
    }

    public static <T> ResultVO<T> ok(Integer code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> ResultVO<T> ok(Integer code, String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.code = code;
        result.message = message;
        result.data = data;
        return result;
    }

    public Boolean success() {
        return code == 0;
    }

}
