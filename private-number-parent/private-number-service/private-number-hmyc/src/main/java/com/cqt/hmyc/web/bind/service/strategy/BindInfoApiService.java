package com.cqt.hmyc.web.bind.service.strategy;

import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;

/**
 * @author linshiqiang
 * @since 2022-11-16 10:09
 */
public interface BindInfoApiService {

    /**
     * 查询绑定关系,
     *
     * @param bindInfoApiQuery 查询参数
     * @return 结果
     */
    ResultVO<BindInfoApiVO> getBindInfo(BindInfoApiQuery bindInfoApiQuery);
}
