package com.cqt.hmyc.web.corpinfo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;

/**
 * @author dingsh
 * date 2022/07/27
 */

public interface IPrivateCorpBusinessInfoService extends IService<PrivateCorpBusinessInfo> {

    /**
     * 根据x号码查询所属企业业务配置
     *
     * @param secretNo x号码
     * @return 企业业务配置
     */
    String getVccId(String secretNo);
}
