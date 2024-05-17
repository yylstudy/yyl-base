package com.repush.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
@Builder
public class MeituanSmsStatePush implements Serializable {
    private String appId;
    private String appkey;
    private String ts;
    private String sign;
    private String sms_id;
    private String area_code;
    private String bind_id;
    private String sender;
    private String receiver;
    private String sender_show;
    private String receiver_show;
    private String transfer_time;
    private String sms_content;
    private String sms_result;
    private String user_data;
    //private String code;
    //private String message;

}
