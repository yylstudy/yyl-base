package com.cqt.hmyc.web.bind.service.ax;

import com.alibaba.fastjson.JSONObject;
import com.cqt.hmyc.config.exception.ParamsException;
import com.cqt.hmyc.web.bind.service.BindStrategy;
import com.cqt.hmyc.web.bind.service.BindStrategyManager;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author huweizhong
 * date  2023/12/8 10:02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AxeBindService {

    private final BindStrategyManager bindStrategyManager;

    public Result binding(AxeBindingDTO bindingDTO, String supplierId) {
        if (log.isInfoEnabled()) {
            log.info("当前第三方 id: {}, 参数: {}", supplierId, JSONObject.toJSONString(bindingDTO));
        }
        // 获取策略
        Optional<BindStrategy> bindStrategyOptional = bindStrategyManager.getBindStrategy(supplierId);
        BindStrategy bindStrategy = bindStrategyOptional.orElseThrow(() -> new ParamsException("供应商id找不到策略实现."));
        return bindStrategy.axeBinding(bindingDTO, supplierId);
    }

    public Result unbind(UnBindDTO unBindDTO, String supplierId) {
        if (log.isInfoEnabled()) {
            log.info("当前第三方 id: {}, 参数: {}", supplierId, JSONObject.toJSONString(unBindDTO));
        }
        // 获取策略
        Optional<BindStrategy> bindStrategyOptional = bindStrategyManager.getBindStrategy(supplierId);
        BindStrategy bindStrategy = bindStrategyOptional.orElseThrow(() -> new ParamsException("供应商id找不到策略实现."));
        return bindStrategy.axeUnbind(unBindDTO, supplierId);
    }


    public Result updateAxeExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId) {
        if (log.isInfoEnabled()) {
            log.info("当前第三方 id: {}, 参数: {}", supplierId, JSONObject.toJSONString(updateExpirationDTO));
        }
        // 获取策略
        Optional<BindStrategy> bindStrategyOptional = bindStrategyManager.getBindStrategy(supplierId);
        BindStrategy bindStrategy = bindStrategyOptional.orElseThrow(() -> new ParamsException("供应商id找不到策略实现."));
        return bindStrategy.updateAxeExpirationBind(updateExpirationDTO, supplierId);
    }
}
