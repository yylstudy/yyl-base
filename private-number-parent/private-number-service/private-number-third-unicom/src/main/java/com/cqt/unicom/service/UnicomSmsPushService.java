package com.cqt.unicom.service;

import com.cqt.model.unicom.dto.SmsStatusDTO;
import com.cqt.model.unicom.entity.MeituanSmsStatePush;
import com.cqt.model.unicom.entity.SmsRequest;
import io.swagger.annotations.Api;

/**
 * @author zhengsuhao
 * @date 2022/12/12
 */
@Api(tags = "联通集团总部(江苏)能力:短信状态服务")
public interface UnicomSmsPushService {

    /**
     * 集团报文转换客户报文
     *
     * @param smsStatusDTO
     * @return SmsRequest
     */
    SmsRequest getSmsStatus(SmsStatusDTO smsStatusDTO);

    /**
     * 报文放入消息队列服务
     *
     * @param smsRequest
     * @return String                      下
     */
    String setMessageQueue(SmsRequest smsRequest);

    /**
     * @param meituanSmsStatePush
     * @return
     */
    String putPrivateNumSms(MeituanSmsStatePush meituanSmsStatePush);

    MeituanSmsStatePush getMeituanSmsStatePush(SmsRequest smsRequest);
}
