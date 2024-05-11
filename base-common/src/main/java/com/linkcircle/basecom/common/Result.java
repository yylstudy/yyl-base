package com.linkcircle.basecom.common;


import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.linkcircle.basecom.config.ApplicationContextHolder;
import com.linkcircle.basecom.constants.CommonConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@Schema(description = "接口返回对象")
public class Result<T> {

    public static final int OK_CODE = CommonConstant.SC_OK_200;

    public static final String OK_MSG = "success";

    private static Integer defaultErrorCode;

    @Schema(description = "状态码")
    private Integer code;
    @Schema(description = "返回处理消息")
    private String message;
    @Schema(description = "返回数据")
    private T data;

    public Result(){

    }
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> ok() {
        return new Result<>(OK_CODE,   OK_MSG, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(OK_CODE,   OK_MSG, data);
    }


    public static <T> Result<T> error(String msg) {
        if(defaultErrorCode==null){
            synchronized (Result.class){
                if(defaultErrorCode==null){
                    defaultErrorCode = ApplicationContextHolder.getEnvironment()
                            .getProperty("result.defaultErrorCode",Integer.class, HttpStatus.HTTP_INTERNAL_ERROR);
                }
            }
        }
        return new Result<>(defaultErrorCode,  msg, null);
    }
    public static <T> Result<T> error(int errorCode, String msg) {
        return new Result<>(errorCode,  msg, null);
    }

    public static <T> Result<T> errorAuth(String msg) {
        return new Result<>(HttpStatus.HTTP_UNAUTHORIZED,  msg, null);
    }
    @JsonIgnore
    public boolean isSuccess() {
        return CommonConstant.SC_OK_200.equals(this.code);
    }

}
