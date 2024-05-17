package com.cqt.model.ext.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:36
 */
@Data
public class ExtStatusTransferDTO implements Serializable {

    private static final long serialVersionUID = -173586938144006935L;

    /**
     * 分机 当前通话的uuid
     */
    private String uuid;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 当前关联的坐席id
     */
    private String agentId;

    /**
     * 分机id
     */
    private String extId;

    /**
     * 当前分机注册地址
     */
    private String regAddr;

    /**
     * 分机ip
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
     * 迁移动作
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

    private String os;

}
