package com.cqt.broadnet.web.x.service;

import cn.hutool.core.util.ObjectUtil;
import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiStrategyFactory {

    private final List<ApiStrategy> apiStrategyList;

    private final ObjectMapper objectMapper;

    private static final Map<String, ApiStrategy> API_STRATEGY_MAP = new ConcurrentHashMap<>(16);

    private static final String METHOD_PARAM = "method";

    @PostConstruct
    public void cache() {
        for (ApiStrategy apiStrategy : apiStrategyList) {
            API_STRATEGY_MAP.put(apiStrategy.getMethod(), apiStrategy);
        }
        log.info("init api strategy finish: {}", API_STRATEGY_MAP.size());
    }

    /**
     * 执行接口逻辑, 请求参数每个接口有差异, 通过参数method调用不同接口实现
     *
     * @param params 接口参数
     * @return 响应结果
     */
    public CallControlResponseVO execute(Map<String, Object> params) throws JsonProcessingException {
        String jsonStr = objectMapper.writeValueAsString(params);
        log.info("method: {}, 呼叫控制请求参数: {}", params.get("method"), jsonStr);
        Object method = params.get(METHOD_PARAM);
        Optional<ApiStrategy> apiStrategyOptional = getApiStrategy(method);
        if (!apiStrategyOptional.isPresent()) {
            log.info("method: {} 找不到策略实现", method);
            return CallControlResponseVO.fail("method错误!");
        }

        ApiStrategy apiStrategy = apiStrategyOptional.get();
        CallControlResponseVO responseVO = apiStrategy.execute(jsonStr);
        if (log.isInfoEnabled()) {
            log.info("method: {}, 呼叫控制响应结果: {}", params.get("method"), objectMapper.writeValueAsString(responseVO));
        }
        return responseVO;
    }

    /**
     * 根据接口获取策略实现
     */
    private Optional<ApiStrategy> getApiStrategy(Object method) {
        if (ObjectUtil.isEmpty(method)) {
            return Optional.empty();
        }
        return Optional.ofNullable(API_STRATEGY_MAP.get(method.toString()));
    }

}
