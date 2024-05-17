package com.cqt.model.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author Xienx
 * date 2023-07-10 09:26:9:26
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("cloudcc_skill_info")
public class SkillInfo implements Serializable {

    private static final long serialVersionUID = -2967736012815027454L;

    /**
     * 技能id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonProperty("skill_id")
    private String skillId;

    /**
     * 技能名称
     */
    @JsonProperty("skill_name")
    private String skillName;

    /**
     * 租户id同企业编码
     */
    private String tenantId;

    /**
     * 排队超时时间（秒）
     */
    private Integer queueTimeout;

    /**
     * 等待音类型(默认音/默认视频和自选)
     */
    private Integer waitingToneType;

    /**
     * 等待音文件id
     */
    private String waitingTone;

    /**
     * 排队策略(字典配置): (1 TIME-排队时长,  2 COMBINE-组合策略)
     */
    private Integer queueStrategy;

    /**
     * 组合策略 规则数据来源 1-外部接口,  2-客户来电优先级
     */
    private Integer priorityDatasource;

    /**
     * 组合策略 客户来电优先级是否启用 1-启用, 0-不启用
     */
    private Integer priorityEnable;

    /**
     * 闲时策略(字典配置)
     */
    private Integer idleStrategy;

}
