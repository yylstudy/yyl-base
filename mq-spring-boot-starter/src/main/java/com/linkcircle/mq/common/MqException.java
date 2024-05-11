package com.linkcircle.mq.common;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/6 14:05
 */

public class MqException extends RuntimeException{
    public MqException(String message){
        super(message);
    }
    public MqException(Throwable cause){
        super(cause);
    }
}
