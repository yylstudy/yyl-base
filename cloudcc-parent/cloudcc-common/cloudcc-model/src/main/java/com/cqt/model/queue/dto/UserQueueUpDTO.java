package com.cqt.model.queue.dto;

import com.cqt.base.enums.MediaStreamEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-19 16:45
 * 用户排队时的数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserQueueUpDTO implements Serializable {

    private static final long serialVersionUID = 4314977686005200455L;

    /**
     * 技能id
     */
    private String skillId;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 来电通话唯一标识
     */
    private String uuid;

    /**
     * 来电号码(主叫号码)
     */
    private String callerNumber;

    /**
     * 企业号码-来电拨打的号码
     */
    private String calleeNumber;

    /**
     * 最长排队时间(s)
     */
    private Integer maxQueueTime;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 当前排队次数
     * 第几次排队
     */
    private Integer currentTimes;

    /**
     * 第一次排队时间戳
     */
    private Long firstTimestamp;

    /**
     * 当前排队时间戳
     */
    private Long currentTimestamp;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 排队分配到坐席的时间戳
     * 为空就是hangup时间戳-没有分配到坐席
     * 若分配到坐席 坐席没接 是否要重新排队?
     */
    private Long successTimestamp;

    /**
     * 语音流
     *
     * @see MediaStreamEnum
     */
    private Integer audio;

    /**
     * 视频流
     *
     * @see MediaStreamEnum
     */
    private Integer video;

    /**
     * 排队策略(字典配置): (1 TIME-排队时长,  2 COMBINE-组合策略)
     */
    private Integer queueStrategy;

    /**
     * 闲时策略(字典配置)
     */
    private Integer idleStrategy;

    /**
     * 组合策略 规则数据来源 1-外部接口,  2-客户来电优先级
     */
    private Integer priorityDatasource;

    /**
     * 组合策略 客户来电优先级是否启用 1-启用, 0-不启用
     */
    private Integer priorityEnable;

    private String waitTone;

    private Integer type;

    /**
     * 构造
     */
    public static UserQueueUpDTO build(CallInIvrActionDTO callInIvrActionDTO) {
        return UserQueueUpDTO.builder()
                .callerNumber(callInIvrActionDTO.getCallerNumber())
                .calleeNumber(callInIvrActionDTO.getCalleeNumber())
                .companyCode(callInIvrActionDTO.getCompanyCode())
                .currentTimestamp(callInIvrActionDTO.getTimestamp())
                .firstTimestamp(callInIvrActionDTO.getFirstTimestamp())
                .maxQueueTime(callInIvrActionDTO.getTimeout())
                .maxRetry(callInIvrActionDTO.getMaxRetry())
                .currentTimes(callInIvrActionDTO.getCurrentTimes())
                .skillId(callInIvrActionDTO.getSkillId())
                .uuid(callInIvrActionDTO.getUuid())
                .audio(callInIvrActionDTO.getAudio())
                .video(callInIvrActionDTO.getVideo())
                .waitTone(callInIvrActionDTO.getSkillWaitTone())
                .type(callInIvrActionDTO.getType())
                .build();
    }
}
