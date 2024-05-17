package com.cqt.cdr.service;

import com.cqt.cdr.entity.AcrRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;

import java.util.ArrayList;

/**
* @author Administrator
* @description 针对表【clouccc_acr_record】的数据库操作Service
* @createDate 2023-08-21 15:26:31
*/
public interface AcrRecordService extends IService<AcrRecord> {
    void insertAcrRecord(String LOG_TAG, String month, ArrayList<AcrRecord> acrRecords, AcrRecord acrRecord);

    AcrRecord getAcrRecord(CallCenterMainCdr mainCdr, CallCenterSubCdr callCenterSubCdr, CdrChanneldata cdrChanneldata) throws Exception;

}
