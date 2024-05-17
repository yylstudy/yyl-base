package com.cqt.model.numpool.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hlx
 * @date 2021-09-11
 */
@Data
public class UtilizationData {
    /**
     * axb号池使用率
     */
    @JsonProperty("axb_utilization")
    List<Utilization> axbUtilization;

    /**
     * axe号池使用率
     */
    @JsonProperty("ax_utilization")
    List<Utilization> axUtilization;
}
