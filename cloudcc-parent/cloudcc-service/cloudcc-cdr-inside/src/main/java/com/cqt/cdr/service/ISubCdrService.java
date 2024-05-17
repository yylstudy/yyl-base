package com.cqt.cdr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.cdr.entity.CallCenterSubCdr;

import java.util.List;


/**
 *
 * @author ld
 * @since 2023-08-15
 */
public interface ISubCdrService extends IService<CallCenterSubCdr> {
     void insertSubCdr(String LOG_TAG, List<CallCenterSubCdr> subCdrs, String companyCode, String month, CallCenterSubCdr subCdr);
}
