package com.cqt.cloudcc.manager.service;

import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.entity.ExtInfo;

/**
 * @author linshiqiang
 * date:  2023-08-22 10:07
 */
public interface ExtInfoService {

    /**
     * 查询分机基本信息
     *
     * @param companyCode 企业id
     * @param extId       系统级分机id
     * @return 分机信息
     */
    ExtInfo getExtInfo(String companyCode, String extId) throws Exception;

    /**
     * 从redis查询分机实时状态
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 分机实时状态
     */
    ExtStatusDTO getActualExtStatus(String companyCode, String extId);

    /**
     * 保存分机实时状态
     *
     * @param extStatusDTO 分机实时状态
     */
    void updateActualExtStatus(ExtStatusDTO extStatusDTO);
}
