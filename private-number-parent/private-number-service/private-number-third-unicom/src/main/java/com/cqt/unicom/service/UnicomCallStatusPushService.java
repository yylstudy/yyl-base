package com.cqt.unicom.service;

import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.unicom.dto.CallBusinessEventDTO;
import com.cqt.model.unicom.dto.CallConnectionStatusDTO;
import io.swagger.annotations.Api;

/**
 * @author zhengsuhao
 * @date 2022/12/10
 */
@Api(tags = "联通集团总部(江苏)能力:通话状态推送服务")
public interface UnicomCallStatusPushService {

    /**
     * 将集团报文转为客户报文
     *
     * @param callConnectionStatusDTO
     * @return PrivateStatusInfo
     */
    PrivateStatusInfo getCustomerCallStatus(CallConnectionStatusDTO callConnectionStatusDTO);

    /**
     * 调用private-num-push服务
     *
     * @param callBusinessEventDTO
     * @return PrivateStatusInfo
     */
    PrivateStatusInfo getCustomerCallStatus(CallBusinessEventDTO callBusinessEventDTO);

    /**
     * String调用private-num-push服务
     *
     * @param privateStatusInfo
     * @return String
     */
    String putPrivateNumPush(PrivateStatusInfo privateStatusInfo);

}
