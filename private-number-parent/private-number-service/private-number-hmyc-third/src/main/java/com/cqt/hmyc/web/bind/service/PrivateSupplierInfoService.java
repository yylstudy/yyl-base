package com.cqt.hmyc.web.bind.service;

import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.web.model.hdh.auth.HdhAuthInfoVO;

/**
 * @author linshiqiang
 * date:  2023-04-13 14:41
 */
public interface PrivateSupplierInfoService {

    /**
     * 获取当前请求url和 鉴权header值
     *
     * @param supplierId      供应商id
     * @param operateTypeEnum 操作类型
     * @return HdhAuthInfoVO
     */
    HdhAuthInfoVO getAuthInfo(String supplierId, OperateTypeEnum operateTypeEnum);

}
