package com.cqt.model.unicom.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhengsuhao
 * @date 2022/12/12
 */
@Data
public class SmsRequest implements Serializable {

    private static final long serialVersionUID = 1982498571902176383L;

    private SmsRequestHeader header;
    private SmsRequestBody body;

    public SmsRequestHeader getHeader() {
        return header;
    }

    public SmsRequestBody getBody() {
        return body;
    }

    public SmsRequest(SmsRequestHeader header, SmsRequestBody body) {
        this.header = header;
        this.body = body;
    }

    public SmsRequest() {
    }
}
