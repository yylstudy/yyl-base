package com.cqt.cloudcc.manager.service;

import com.cqt.base.enums.DefaultToneEnum;
import com.cqt.model.company.entity.CompanyInfo;

import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-08-28 14:04
 */
public interface CompanyInfoService {

    /**
     * 查询企业信息
     *
     * @param companyCode 企业id
     * @return 企业信息
     */
    CompanyInfo getCompanyInfoDTO(String companyCode);

    /**
     * 获取全部启用的企业id集合
     *
     * @return 企业id集合
     */
    Set<String> getEnableCompanyCode();

    /**
     * 获取全部的企业id集合
     *
     * @return 企业id集合
     */
    Set<String> getAllCompanyCode();

    /**
     * 查询平台默认音配置-根据类型
     */
    String getPlatformDefaultTone(DefaultToneEnum defaultTone);

}
