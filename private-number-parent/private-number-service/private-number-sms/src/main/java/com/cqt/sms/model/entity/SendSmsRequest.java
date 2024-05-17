/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.cqt.sms.model.entity;

import lombok.Data;

/**
 *
 */
@Data
public class SendSmsRequest {
    private SmsRequestHeader header;
    private SendSmsRequestBody body;

    public SmsRequestHeader getHeader() {
        return header;
    }

    public SendSmsRequestBody getBody() {
        return body;
    }

}
