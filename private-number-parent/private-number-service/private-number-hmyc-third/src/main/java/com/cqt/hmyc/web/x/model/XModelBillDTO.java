package com.cqt.hmyc.web.x.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author huweizhong
 * date  2023/6/7 15:58
 */
@Data
public class XModelBillDTO {

    private String vendorKey;

    private String callId;

    private String bindId;

    private String callNo;

    private String peerNo;

    private String secretNo;

    private String displayNo;

    private String callTime;

    private String ringTime;

    private String forwardTime;

    private String startTime;

    private String finishTime;

    private Integer callDuration;

    private String finishType;

    private String finishState;

    private String data;

    private String extensionNo;

    private String playedEarlyVideo;

}
