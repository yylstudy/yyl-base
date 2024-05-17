/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.cqt.sms.model.entity;

import lombok.Data;

/**
 * @ClassName: SmsRequest
 * @Description: TODO
 * @author: youngder
 * @date: 2017年11月17日 下午1:00:26  
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
