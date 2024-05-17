package com.cqt.unicom.dto;

import lombok.Data;

/**
 * @author huweizhong
 * date  2023/7/6 11:14
 * 联通查询AXE绑定关系
 */
@Data
public class QueryAxeBindDTO {

    private String appId;

    private String timestamp;

    private String sign;

    private String callId;

    private String dtmfValue;

    private String dtmfTime;

    private String uId;
}
