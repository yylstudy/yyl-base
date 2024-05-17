package com.cqt.monitor.web.callevent.xxjob;

/**
 * @description:
 * @author: yang.yonglian
 * @create: 2021-07-08
 **/
public class BusinessException extends RuntimeException {

    public BusinessException(String message){
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message,cause);
    }

}
