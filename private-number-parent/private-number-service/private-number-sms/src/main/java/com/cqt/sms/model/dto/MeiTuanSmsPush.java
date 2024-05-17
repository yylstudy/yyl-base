package com.cqt.sms.model.dto;

import lombok.Data;

/**
 * @author huweizhong
 * date  2023/10/7 16:36
 */
@Data
public class MeiTuanSmsPush {
    private String appId;
    private String appkey;
    private Long ts;
    private String sign;
    private String sms_id;
    private String area_code;
    private String bind_id;
    private String sender;
    private String receiver;
    private String sender_show;
    private String receiver_show;
    private int transfer_time;
    private String sms_content;
    private int sms_result;
    private String user_data;
}
