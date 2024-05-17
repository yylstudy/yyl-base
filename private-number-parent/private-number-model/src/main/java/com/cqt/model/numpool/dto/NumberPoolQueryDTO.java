package com.cqt.model.numpool.dto;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * NumberPoolQueryDTO
 *
 * @author Xienx
 * @date 2023年04月04日 13:36
 */
@Data
@JsonFormat
@ApiModel(value = "号码池查询接口")
public class NumberPoolQueryDTO implements Serializable {

    private static final long serialVersionUID = 6404967588825787460L;

    /**
     * 号码池归属区号 如010
     */
    @JsonProperty("area_code")
    @ApiModelProperty(value = "号码池归属区号", example = "010")
    private String areaCode;

    /**
     * 号码池类型
     */
    @NotEmpty(message = "号码池类型不能为空")
    @JsonProperty("pool_type")
    @ApiModelProperty(value = "号码池类型", example = "AXE")
    private String poolType;

    /**
     * 分页页码
     */
    @NotNull(message = "查询页数参数不能为空")
    @JsonProperty("page_num")
    @Min(message = "查询页数, 从1开始", value = 1L)
    @ApiModelProperty(value = "查询页数, 最小值为0", example = "1")
    private Integer pageNum;

    /**
     * 每页查询大小
     */
    @JsonProperty("page_size")
    @Min(message = "每页查询条数, 最小为1条", value = 1L)
    @Max(message = "每页查询条数, 最大为100条", value = 100L)
    @ApiModelProperty(value = "每页查询条数, 1~100, 缺省100", example = "50")
    private Integer pageSize = 50;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
