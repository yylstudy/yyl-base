package com.cqt.model.unicom.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhengsuhao
 * @date 2022/12/12
 */
@Data
public class SmsRequestHeader implements Serializable {

    private static final long serialVersionUID = -9210096535794929636L;

    /*
    {
        "header": {
            "streamNumber":"1c7799203edf9c71d1b2762dc68725b9",
            "messageId":"35135rgsetgqw341qgsd24t"
        },
        "body": {
            "aPhoneNumber": "18559106605",
            "inPhoneNumber": "18559752475",
            "gsmCenter": "008613010112500",
            "inContent": "测试短信。"
        }
    }
    */

    /**
     * 流水号
     */
    private String streamNumber;
    /**
     * 业务流水号
     */
    private String messageId;
    /**
     * 短信标识
     */
    private String messageReference;

    public SmsRequestHeader(String streamNumber, String messageId, String messageReference) {
        this.streamNumber = streamNumber;
        this.messageId = messageId;
        this.messageReference = messageReference;
    }

    public SmsRequestHeader() {
    }
}