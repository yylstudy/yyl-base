package com.cqt.cloudcc.manager.service;

import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.model.number.entity.NumberInfo;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-09-04 15:53
 */
public interface NumberInfoService {

    /**
     * 查询号码信息
     *
     * @param number 号码
     * @return 号码信息
     */
    Optional<NumberInfo> getNumberInfo(String number);

    /**
     * 查询来电号码优先级
     *
     * @param companyCode  企业号码
     * @param callerNumber 来电号码
     * @return 用户等级
     */
    Integer getClientPriority(String companyCode, String callerNumber);

    /**
     * 检测号码是否在黑名单内
     *
     * @param companyCode   企业id
     * @param number        号码
     * @param callDirection 呼叫方向
     * @return 是否在黑名单内
     */
    Boolean checkBlackNumber(String companyCode, String number, CallDirectionEnum callDirection);
}
