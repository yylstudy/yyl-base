package com.cqt.hmyc.web.service;

import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.call.vo.TaobaoBindInfoVO;

/**
 * @author linshiqiang
 * date:  2023-01-28 11:03
 */
public interface BindInfoQueryService {

    /**
     * taobao 查询绑定关系
     *
     * @param bindInfoApiQuery 查询条件
     * @return 绑定关系
     */
    TaobaoBindInfoVO getBindInfo(BindInfoApiQuery bindInfoApiQuery);

}
