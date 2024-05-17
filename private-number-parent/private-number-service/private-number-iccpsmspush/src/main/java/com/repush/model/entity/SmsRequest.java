/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.repush.model.entity;

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
