package com.repush.dao.domain;

import lombok.Data;

/**
 *
 */
@Data
public class SmsFailedRequest {
    private String id;
    private String ip;
    private String vccId;
    private String url;
    private String json;
    private String state;
    private String createTime;
    private String updateTime;
    private int repushCount;
    private String failCode;
    private String failedReason;
}
