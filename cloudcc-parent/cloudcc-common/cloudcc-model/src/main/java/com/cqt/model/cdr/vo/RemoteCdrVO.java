package com.cqt.model.cdr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RemoteCdrVO  implements Serializable {
    private static final long serialVersionUID = -6842057420803459739L;
    private String code;
    private ResponseData obj;

    @Data
    class ResponseData {
        //响应id	和请求的id相同
        private String responseId;
        //错误编码	0：成功 1：失败 2：部分成功
        private String errorCode;
        //异常原因	0：成功 1：失败 2：部分成功
        private String errorMessage;
    }
}
