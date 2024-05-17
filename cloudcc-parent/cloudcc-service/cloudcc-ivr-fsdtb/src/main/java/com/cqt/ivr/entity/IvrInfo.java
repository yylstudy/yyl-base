package com.cqt.ivr.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * ivr信息
 *
 * Created by xinson
 */
@Data
@ApiModel("ivr信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IvrInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * ivrId
     */
    @ApiModelProperty(value = "ivrId")
    private String ivrId;

    /**
     * ivr名称
     */
    @ApiModelProperty(value = "ivr名称")
    private String ivrName;

}
