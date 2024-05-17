package com.cqt.hmbc.service;

import com.cqt.model.hmbc.dto.CdrRecordSimpleEntity;

/**
 * AcrRecordService
 *
 * @author Xienx
 * @date 2023年02月10日 9:43
 */
public interface AcrRecordService {

    /**
     * 根据数据源动态查询话单数据
     *
     * @param cdrRecord 话单数据
     * @param dsName    数据源名称
     * @return Long 符合条件的记录数
     */
    Long findAcr(CdrRecordSimpleEntity cdrRecord, String dsName);


    /**
     * 根据号码查询出对应的Gt编码
     *
     * @param number 号码
     * @return String 对应的gt编码
     */
    String getGtCodeByNumber(String number);
}
