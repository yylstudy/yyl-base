package com.cqt.hmyc.web.bind.service.axb;

import com.alibaba.fastjson.JSONObject;
import com.cqt.hmyc.config.exception.ParamsException;
import com.cqt.hmyc.web.bind.service.BindStrategy;
import com.cqt.hmyc.web.bind.service.BindStrategyManager;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


/**
 * 第三方绑定服务类
 *
 * @author dingsh
 * date 2022/07/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AxbBindService {

    private final BindStrategyManager bindStrategyManager;

    public Result binding(AxbBindingDTO bindingDTO, String supplierId) {
        if (log.isInfoEnabled()) {
            log.info("当前第三方 id: {}, 参数: {}", supplierId, JSONObject.toJSONString(bindingDTO));
        }
        // 获取策略
        Optional<BindStrategy> bindStrategyOptional = bindStrategyManager.getBindStrategy(supplierId);
        BindStrategy bindStrategy = bindStrategyOptional.orElseThrow(() -> new ParamsException("供应商id找不到策略实现."));
        return bindStrategy.binding(bindingDTO, supplierId);
    }

    public Result unbind(UnBindDTO unBindDTO, String supplierId) {
        if (log.isInfoEnabled()) {
            log.info("当前第三方 id: {}, 参数: {}", supplierId, JSONObject.toJSONString(unBindDTO));
        }
        // 获取策略
        Optional<BindStrategy> bindStrategyOptional = bindStrategyManager.getBindStrategy(supplierId);
        BindStrategy bindStrategy = bindStrategyOptional.orElseThrow(() -> new ParamsException("供应商id找不到策略实现."));
        return bindStrategy.unbind(unBindDTO, supplierId);
    }

    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId) {
        if (log.isInfoEnabled()) {
            log.info("当前第三方 id: {}, 参数: {}", supplierId, JSONObject.toJSONString(updateExpirationDTO));
        }
        // 获取策略
        Optional<BindStrategy> bindStrategyOptional = bindStrategyManager.getBindStrategy(supplierId);
        BindStrategy bindStrategy = bindStrategyOptional.orElseThrow(() -> new ParamsException("供应商id找不到策略实现."));
        return bindStrategy.updateExpirationBind(updateExpirationDTO, supplierId);
    }

}
