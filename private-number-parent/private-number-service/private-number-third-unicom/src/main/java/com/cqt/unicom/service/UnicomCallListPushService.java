package com.cqt.unicom.service;

import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.unicom.dto.CallListPushDTO;
import com.cqt.model.unicom.entity.CustomerReceivesDataInfo;
import io.swagger.annotations.Api;

/**
 * @author zhengsuhao
 * @date 2022/12/7
 */
@Api(tags = "联通集团总部(江苏)能力:话单推送服务")
public interface UnicomCallListPushService {

    /**
     * 将集团报文转为客户报文
     *
     * @param callListPushDTO
     * @return CustomerReceivesDataInfo
     */
    CustomerReceivesDataInfo getCustomerReceivesDataInfo(CallListPushDTO callListPushDTO);

    /**
     * 将json放入MQ
     *
     * @param customerReceivesDataInfo
     * @return String
     */
    String setMessageQueue(CustomerReceivesDataInfo customerReceivesDataInfo);

    /**
     * 调用COMMCdrPUSH服务
     *
     * @param customerReceivesDataInfo
     * @return String
     */
    String pushCommCdrPushService(CustomerReceivesDataInfo customerReceivesDataInfo);

    PrivateBillInfo getPrivateBillInfo(CustomerReceivesDataInfo acr);

    public String getVccid(String phoneNumberX);
}
