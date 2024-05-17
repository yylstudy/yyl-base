package com.cqt.model.numpool.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author hlx
 * @date 2021-09-11
 */
@Data
public class Utilization {

    /**
     * 区号
     */
    @JsonProperty("area_code")
    private String areaCode;

    /**
     * 使用率
     */
    private float utilization;

    /**
     * 计算使用率的分子
     */
    @JsonProperty("utilization_fz")
    private int utilizationFz;

    /**
     * 计算使用率的分母
     */
    @JsonProperty("utilization_fm")
    private int utilizationFm;


}
