package com.cqt.model.cdr.dto;

import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-07 14:30
 * 一通通话的信息记录
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallCdrDTO implements Serializable {

    private static final long serialVersionUID = -9068433660980389712L;

    /**
     * 通话id
     */
    private String uuid;

    /**
     * 呼入事件
     */
    private Boolean callInFlag;

    /**
     * 呼入时间戳
     */
    private Long calInTimestamp;

    /**
     * 外呼事件
     */
    private Boolean inviteFlag;

    /**
     * 外呼时间戳
     */
    private Long inviteTimestamp;

    /**
     * 振铃事件
     */
    private Boolean ringFlag;

    /**
     * 振铃时间戳
     */
    private Long ringTimestamp;

    /**
     * 接通事件
     */
    private Boolean answerFlag;

    /**
     * 接通时间戳
     */
    private Long answerTimestamp;

    /**
     * 桥接事件
     */
    private Boolean bridgeFlag;

    /**
     * 桥接时间戳
     */
    private Long bridgeTimestamp;

    /**
     * 挂断事件
     */
    private Boolean hangupFlag;

    /**
     * 挂断时间戳
     */
    private Long hangupTimestamp;

    /**
     * 挂断原因-自定义
     */
    private HangupCauseEnum hangupCauseEnum;

    /**
     * 录制文件
     */
    private String recordFileName;

    /**
     * fs底层给的挂断原因
     */
    private String hangupCause;

    /**
     * fs空号检测给的原因
     */
    private String da2Result;
}
