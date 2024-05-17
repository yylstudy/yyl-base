package com.cqt.sms.service;

import com.cqt.model.unicom.entity.SmsRequest;
import org.springframework.scheduling.annotation.Async;

/**
 * @author zhengsuhao
 * @date 2023/2/6
 */
public interface QingHaiAliSmsInterface {


    /**
     * 阿里短信处理
     *
     * @param smsRequest 上行短信参数
     */
    @Async("executor")
    void smsHandle(SmsRequest smsRequest);


}
