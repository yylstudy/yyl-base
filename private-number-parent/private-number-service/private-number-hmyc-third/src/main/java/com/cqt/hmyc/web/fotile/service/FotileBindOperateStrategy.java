package com.cqt.hmyc.web.fotile.service;

import com.cqt.hmyc.web.fotile.model.dto.BindOperationDTO;
import com.cqt.hmyc.web.fotile.model.vo.BindOperationResultVO;

/**
 * @author linshiqiang
 * @date 2022/7/22 16:18
 * 方太绑定操作策略接口
 */
public interface FotileBindOperateStrategy {

    /**
     * 处理
     *
     * @param bindOperationDTO 绑定操作参数
     * @param vccId            企业id
     * @return 结果
     */
    BindOperationResultVO deal(BindOperationDTO bindOperationDTO, String vccId);

    /**
     * 操作类型
     *
     * @return 操作类型
     */
    Integer getOperationType();

}
