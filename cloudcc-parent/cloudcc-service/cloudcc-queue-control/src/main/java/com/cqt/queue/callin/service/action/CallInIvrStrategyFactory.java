package com.cqt.queue.callin.service.action;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.exception.BizException;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-08-21 10:27
 * 呼入ivr转接动作策略工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallInIvrStrategyFactory implements CommandLineRunner {

    public static final Map<Integer, CallInIvrStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<CallInIvrStrategy> callInIvrStrategyList;

    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        for (CallInIvrStrategy strategy : callInIvrStrategyList) {
            STRATEGY_MAP.put(strategy.getAction().getCode(), strategy);
        }
        log.info("初始化呼入ivr转接动作策略: {}", STRATEGY_MAP.size());
    }

    /**
     * 执行具体策略
     *
     * @param callInIvrActionDTO 呼入ivr参数
     */
    public ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) throws Exception {
        Integer type = callInIvrActionDTO.getType();
        try {
            String traceId = TraceIdUtil.buildTraceId(type, callInIvrActionDTO.getCompanyCode(),
                    callInIvrActionDTO.getSkillId(), callInIvrActionDTO.getUuid());
            TraceIdUtil.setTraceId(traceId);
            if (log.isInfoEnabled()) {
                log.info("[呼入ivr] 类型: {}, 参数: {}", type, objectMapper.writeValueAsString(callInIvrActionDTO));
            }
            CallInIvrStrategy strategy = STRATEGY_MAP.get(type);
            if (Optional.ofNullable(strategy).isPresent()) {
                return strategy.execute(callInIvrActionDTO);
            }
        } finally {
            TraceIdUtil.remove();
        }
        throw new BizException(SdkErrCode.TYPE_NOT_SUPPORT);
    }
}
