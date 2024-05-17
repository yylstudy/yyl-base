package com.cqt.sms.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @description TODO
 * @author youngder
 * @date 2022/2/24 10:45 AM
 */
@Data
@Builder
public class MeituanSmsStatePush implements Serializable {
    //必选	为接入方分配的appkey
    private String appkey;
    //UNIXTIME时间戳，单位为毫秒。
    private Long ts;
    private String sign;
    //本次短信唯一标识
    private String sms_id;
    //区号
    private String area_code;
    //绑定ID(号码绑定时返回的绑定ID)
    private String bind_id;
    //发送者真实号码
    private String sender;
    //接收者真实号码
    private String receiver;
    //发送者分配号码
    private String sender_show;
    //接收者分配号码
    private String receiver_show;
    //短信发送时刻 UNIXTIME单位为秒
    private String transfer_time;
    //业务编码
    //10: axb
    //11: axbn
    //20: axyb-ax
    //21: axyb-ayb
    //22: ax
    private Integer service_code;
    //短信内容
    private String sms_content;
    //短信状态
    //0：成功
    //1：无绑定关系
    private Integer sms_result;
    //绑定请求中携带的请求id
    private String request_id;
    //用户透传数据
    private Object user_data;

    /**
     *  (第三方) 短信条数
     **/
    private Integer sms_number;
}
