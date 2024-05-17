package com.cqt.ivr.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务实体类
 *
 * Created by xinson
 */
@Data
@ApiModel("坐席实体类")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueAgentInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 坐席名称
     */
    @ApiModelProperty(value = "坐席名称")
    private String agentName;

    /**
     * 坐席
     */
    @ApiModelProperty(value = "坐席工号")
    private String agentId;

    /**
     * 坐席状态
     */
    @ApiModelProperty(value = "坐席状态")
    private String agentStatus;

}