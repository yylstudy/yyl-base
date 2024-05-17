package com.cqt.cdr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.cdr.entity.CallCenterMainCdr;


/**
 *
 * @author ld
 * @since 2023-08-15
 */
public interface IMainCdrService extends IService<CallCenterMainCdr> {
     void inserMainCdr(String LOG_TAG, CallCenterMainCdr mainCdr, String companyCode, String month);
}
