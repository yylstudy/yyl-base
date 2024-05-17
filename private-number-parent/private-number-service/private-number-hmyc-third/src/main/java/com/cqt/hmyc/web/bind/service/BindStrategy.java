package com.cqt.hmyc.web.bind.service;

import com.cqt.model.bind.ax.dto.AxBindingDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;

/**
 * @author dingsh
 * @since 2022-07-06
 * 绑定策略
 */
public interface BindStrategy {

    /**
     * axb绑定接口
     *
     * @param axbBindingDTO 绑定消息
     * @return Result
     */
    Result binding(AxbBindingDTO axbBindingDTO, String supplierId);

    /**
     * ax绑定接口
     *
     * @param axBindingDTO 绑定消息
     * @return Result
     */
    Result axeBinding(AxeBindingDTO axBindingDTO, String supplierId);

    /**
     * 解除绑定接口
     *
     * @param unBindDTO 解绑消息体
     * @return Result
     */
    Result unbind(UnBindDTO unBindDTO, String supplierId);

    Result axeUnbind(UnBindDTO unBindDTO, String supplierId);

    /**
     * 更新绑定失效时间接口
     *
     * @param updateExpirationDTO 更新消息体
     * @return Result
     */
    Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId);

    /**
     * 传入的供应商id是否符合该策略
     *
     * @param supplierId 供应商id
     * @return 是否符合
     */
    Boolean match(String supplierId);

    Result updateAxeExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId);
}
