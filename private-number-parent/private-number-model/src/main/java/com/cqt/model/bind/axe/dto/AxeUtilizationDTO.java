package com.cqt.model.bind.axe.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author linshiqiang
 * date:  2023-02-23 17:21
 * AXE分机号余量查询 参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AxeUtilizationDTO extends BaseAuth {

    @JsonProperty("area_code")
    private String areaCode;
}
