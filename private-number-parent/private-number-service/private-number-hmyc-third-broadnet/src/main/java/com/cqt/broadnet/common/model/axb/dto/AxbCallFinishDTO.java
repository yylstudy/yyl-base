package com.cqt.broadnet.common.model.axb.dto;

import lombok.Data;

/**
 * @author huweizhong
 * date  2023/5/26 14:21
 */
@Data
public class AxbCallFinishDTO {

    private String bindId;

    private String callId;

    private Integer cityId;

    private String callNo;

    private String peerNo;

    private String x;

    private Long callTime;

    private Long ringTime;

    private Long startTime;

    private Long finishTime;

    private Integer finishType;

    private Integer finishState;

    private String preX;



}
