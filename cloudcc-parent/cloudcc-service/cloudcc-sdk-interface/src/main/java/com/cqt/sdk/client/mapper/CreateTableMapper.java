package com.cqt.sdk.client.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * @author linshiqiang
 * date:  2023-09-04 17:09
 */
@DS("cdr")
public interface CreateTableMapper {

    /**
     * 创建主话单表
     *
     * @param companyCode 企业id
     * @param month       月份
     */
    void createMainCdrTable(String companyCode, String month);

    /**
     * 创建子话单表
     *
     * @param companyCode 企业id
     * @param month       月份
     */
    void createSubCdrTable(String companyCode, String month);

    /**
     * 创建坐席状态迁移表
     *
     * @param companyCode 企业id
     * @param month       月份
     */
    void createAgentStatusTable(String companyCode, String month);

    /**
     * 创建分机状态迁移表
     *
     * @param companyCode 企业id
     * @param month       月份
     */
    void createExtStatusTable(String companyCode, String month);

    /**
     * 创建通道变量表
     *
     * @param companyCode 企业id
     * @param month       月份
     */
    void createChannelDataTable(String companyCode, String month);

    /**
     * 创建acr_record
     *
     * @param vccId vcc_id
     * @param month 月份
     */
    void createAcrRecordTable(String vccId, String month);
}
