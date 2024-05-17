package com.cqt.model.freeswitch.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-08-01 17:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExecuteLuaVO extends FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = -9035203034426164556L;

    /**
     * execute_lua 执行脚本返回值
     */
    @JsonProperty("return_value")
    private String returnValue;
}
