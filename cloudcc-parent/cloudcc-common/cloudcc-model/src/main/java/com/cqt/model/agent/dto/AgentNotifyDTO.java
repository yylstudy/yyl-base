package com.cqt.model.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:45
 * 坐席被删除通知SDK-Interface
 * 拥有同一技能的空闲坐席队列, 清除被删除的坐席
 * {
 * operateType:  delete, add
 * agentId: '',    坐席id
 * serviceMode: 1,  服务模式
 * ownSkillId: []   坐席拥有技能的id列表
 * }
 */
@Data
public class AgentNotifyDTO implements Serializable {

    private static final long serialVersionUID = -2614795343484610579L;

    @ApiModelProperty("企业id")
    private String companyCode;

    @ApiModelProperty("坐席id")
    private String agentId;

    /**
     * 服务模式 1-客户型 2-外呼型
     */
    @ApiModelProperty("服务模式 1-客户型 2-外呼型")
    private Integer serviceMode;

    @ApiModelProperty("拥有的技能id列表")
    private List<String> ownSkillIdList;
}
