package com.cqt.model.ext.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:23
 */
@Data
public class ExtStatusDTO implements Serializable {

    private static final long serialVersionUID = 1430276355636917208L;

    /**
     * 主叫侧uuid
     */
    private String uuid;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 分机id
     */
    private String extId;

    /**
     * 分机ip  - reg_addr
     */
    private String extIp;

    /**
     * 源 分机状态
     */
    private String sourceStatus;

    /**
     * 源 分机状态时间戳
     */
    private Long sourceTimestamp;

    /**
     * 状态迁移动作类型
     */
    private String transferAction;

    /**
     * 目标 分机状态
     */
    private String targetStatus;

    /**
     * 目标 分机状态时间戳
     */
    private Long targetTimestamp;

    /**
     * 状态变更原因
     */
    private String reason;

    private String os;
}
