package com.cqt.model.numpool.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 号码池查询响应信息
 *
 * @author Xienx
 * @date 2023年04月04日 13:48
 */
@Data
@Accessors(chain = true)
public class NumberPoolVO implements Serializable {

    private static final long serialVersionUID = -2504717081510379099L;

    @JsonProperty("area_code")
    private String areaCode;

    @JsonProperty("num_list")
    private List<String> numList;

    @JsonProperty("total")
    private Long total;
}
