package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:04
 * 查询分机当前的注册状态
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetExtensionRegStatusDTO  extends FreeswitchApiBase implements Serializable {

    /**
     * 是  | 分机号
     */
    @JsonProperty("ext_id")
    private String extId;
}
