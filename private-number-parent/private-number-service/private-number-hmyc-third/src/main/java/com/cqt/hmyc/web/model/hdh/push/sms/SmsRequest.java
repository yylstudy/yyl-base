/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.cqt.hmyc.web.model.hdh.push.sms;

import lombok.Data;

/**
 *
 */
@Data
public class SmsRequest {
    private SmsRequestHeader header;
    private SmsRequestBody body;

    public SmsRequestHeader getHeader() {
        return header;
    }

    public SmsRequestBody getBody() {
        return body;
    }

}
