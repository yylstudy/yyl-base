/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.cqt.model.sms.save;

import lombok.Data;

/**
 * @author CQT
 */
@Data
public class SmsRequest {
    private SmsRequestHeader header;
    private SmsRequestBody body;

}
