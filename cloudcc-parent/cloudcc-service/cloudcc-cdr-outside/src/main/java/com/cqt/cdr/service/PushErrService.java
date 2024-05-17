package com.cqt.cdr.service;

import com.cqt.cdr.entity.PushErr;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【cc_push_err】的数据库操作Service
* @createDate 2023-08-28 16:26:02
*/
public interface PushErrService extends IService<PushErr> {
    public boolean errIntoErrTable(String json, String url, String reason, String month, String companyCode, String LOG_TAG);
    public boolean createTable(String tablename, String yearmonth, String companycode, String LOG_TAG);
}
