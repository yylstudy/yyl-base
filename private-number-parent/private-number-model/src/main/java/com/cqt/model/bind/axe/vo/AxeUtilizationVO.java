package com.cqt.model.bind.axe.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * date:  2023-02-23 17:21
 * AXE分机号余量查询 返回
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxeUtilizationVO {

    @JsonProperty("area_code")
    private String areaCode;

    @JsonProperty("used_count")
    private Long usedCount;

    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("utilization_rate")
    private Double utilizationRate;

}
