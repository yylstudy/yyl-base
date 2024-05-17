package com.cqt.cdr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;;


/**
 *
 * @author ld
 * @since 2023-08-15
 */
public interface ICdrChanneldataService extends IService<CdrChanneldata> {
     void insertCdrChannel(String LOG_TAG, CallCenterMainCdr mainCdr, CdrChanneldata cdrChanneldata, String companyCode, String month);
}
