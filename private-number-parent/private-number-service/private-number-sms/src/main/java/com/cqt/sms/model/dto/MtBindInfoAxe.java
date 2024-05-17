package com.cqt.sms.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 美团小号 AXE 分机号绑定关系(MtBindInfoAxe)实体类
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:35
 */
@Data
public class MtBindInfoAxe implements Serializable {

    /**
     * code : 200
     * message : AYB查询成功!
     * ts : sdadasdasdasdada
     * sign : sdadasdasdasdada
     * request_id : sdadasdasdasdada
     * called_num : 18649760218
     * caller_ivr :
     * called_ivr :
     * enable_record :
     * user_data :
     * bind_id : cqt-axe-1450044249741983744
     * num_type : AYB
     * max_duration : 7200
     * relate_bind_id : cqt-ayb-1450273354336960512
     * bind_time : 2021-10-18 18:21:04
     * call_type : 11
     * tel_y : 13142895074
     * area_code : 0591
     */

    private int code;
    private String message;
    private String ts;
    private String sign;
    private String request_id;
    private String called_num;
    private String caller_ivr;
    private String called_ivr;
    private String enable_record;
    private String user_data;
    private String bind_id;
    private String num_type;
    private int max_duration;
    private String relate_bind_id;
    private String bind_time;
    private String call_type;
    private String tel_y;
    private String area_code;
}
