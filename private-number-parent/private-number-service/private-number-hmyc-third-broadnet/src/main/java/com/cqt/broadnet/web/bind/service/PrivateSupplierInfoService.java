package com.cqt.broadnet.web.bind.service;

import com.cqt.model.supplier.PrivateSupplierInfo;

/**
 * @author linshiqiang
 * date:  2023-04-13 14:41
 */
public interface PrivateSupplierInfoService {

    /**
     * 根据供应商id查询供应商配置信息
     *
     * @param supplierId 供应商id
     * @return PrivateSupplierInfo
     */
    PrivateSupplierInfo getSupplierInfo(String supplierId);
}
