package com.cqt.hmyc.web.bind.service.strategy;

import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:15
 * 绑定关系查询策略接口
 */
public interface BindInfoQueryStrategy {

    /**
     * 查询绑定关系
     *
     * @param bindInfoQuery            bindInfoQuery
     * @param corpBusinessInfoOptional 业务配置信息
     * @return CustomerBindInfoVO
     */
    Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional);

    /**
     * 获得号码类型
     * 应该根据业务模式
     *
     * @return 号码类型
     */
    String getBusinessType();
}
