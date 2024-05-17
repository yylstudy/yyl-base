package com.cqt.cdr.cloudccsfaftersales.service;

import com.cqt.cdr.cloudccsfaftersales.entity.PushErr;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【cc_push_err】的数据库操作Service
* @createDate 2023-09-08 14:13:57
*/
public interface PushErrService extends IService<PushErr> {
    /**
     * 错误信息入库
     *
     * @param json
     * @param url
     * @param reason
     * @param month
     * @param companyCode
     * @param LOG_TAG
     * @return
     */
    public boolean errIntoErrTable(String json, String url,  String reason, String month, String companyCode, String LOG_TAG,String type);

    /**
     * 创建表
     *
     * @param tablename
     * @param yearmonth
     * @param companycode
     * @param LOG_TAG
     * @return
     */
    public boolean createTable(String tablename, String yearmonth, String companycode, String LOG_TAG);
}
