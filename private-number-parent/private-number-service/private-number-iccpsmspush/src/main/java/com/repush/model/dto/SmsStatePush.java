package com.repush.model.dto;

import lombok.Data;

/**
 * @author fat boy y
 */
@Data
public class SmsStatePush {

    private String id;
    private String vccid;
    private String url;
    private String ip;
    private String num;
    private String errMsg;
    private String json;


}
