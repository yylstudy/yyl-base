package com.cqt.cdr.cloudccsfaftersales.entity.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Xienx
 * @date 2023-07-11 14:45:14:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentInfoQueryDTO extends BasePageQuery<AgentInfoQueryVO> {

    private static final long serialVersionUID = -1313703866941138223L;

    /**
     * 开始工号
     */
    private Long startAgentId;

    /**
     * 结束工号
     */
    private Long endAgentId;

    /**
     * 开始分机号
     */
    private Long startExtId;

    /**
     * 结束分机号
     */
    private Long endExtId;

    /**
     * 分机注册方式 1、webrtc 2、第三方话机
     */
    private Integer extRegMode;

    /**
     * 坐席姓名
     */
    private String agentName;

    /**
     * 角色id字符串数组
     */
    private String roleIds;

    /**
     * 班组id字符串数组
     */
    private String departIds;

    /**
     * 技能组id字符串数组
     */
    private String skillPackIds;

    /**
     * 技能id字符串数组
     */
    private String skillIds;

    /**
     * 外显号
     */
    private String displayNumber;

    /**
     * 坐席状态
     */
    private Integer state;
}
