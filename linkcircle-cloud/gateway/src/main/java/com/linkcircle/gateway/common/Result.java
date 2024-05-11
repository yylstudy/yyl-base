package com.linkcircle.gateway.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/20 15:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Integer code;
    private String message;
    @JsonIgnore
    public boolean isSuccess() {
        return GlobalConstants.SC_OK_200.equals(this.code);
    }
    public static Result errorAuth(String msg) {
        return new Result(HttpStatus.UNAUTHORIZED.value(),  msg);
    }
}
