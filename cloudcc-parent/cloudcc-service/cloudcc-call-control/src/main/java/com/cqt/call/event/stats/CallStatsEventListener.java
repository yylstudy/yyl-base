package com.cqt.call.event.stats;

import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.cloudcc.manager.config.CommonThreadPoolConfig;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-10-11 13:41
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallStatsEventListener {

    private final CommonDataOperateService commonDataOperateService;

    /**
     * 监听
     */
    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @EventListener(classes = {CallStatsEvent.class})
    public void listener(CallStatsEvent event) {
        try {
            stats(event);
        } catch (Exception e) {
            log.error("CallStatsEventListener stats error", e);
        }
    }

    private void stats(CallStatsEvent event) {
        String agentId = event.getAgentId();
        CallUuidContext callUuidContext = event.getCallUuidContext();
        CallStatusEventDTO callStatusEventDTO = event.getCallStatusEventDTO();
        Long timestamp = callStatusEventDTO.getTimestamp();
        String companyCode = callStatusEventDTO.getCompanyCode();
        String status = callStatusEventDTO.getData().getStatus();
        if (CallStatusEventEnum.ANSWER.name().equals(status)) {
            // 坐席通话次数累加
            commonDataOperateService.addCallCount(companyCode, agentId, timestamp);

            // 技能
            Optional<Set<String>> skillIdSetOptional = getSkillIdSetOptional(agentId);
            if (skillIdSetOptional.isPresent()) {
                Set<String> skillIdSet = skillIdSetOptional.get();
                for (String skillId : skillIdSet) {
                    commonDataOperateService.addCallCount(companyCode, agentId, skillId, timestamp);
                }
            }
            return;
        }

        if (CallStatusEventEnum.HANGUP.name().equals(status)) {
            // 通话时间累计
            Double duration = getCallTIme(callUuidContext);
            if (duration == 0) {
                return;
            }
            commonDataOperateService.addCallTime(companyCode, agentId, timestamp, duration);

            // 技能
            Optional<Set<String>> skillIdSetOptional = getSkillIdSetOptional(agentId);
            if (skillIdSetOptional.isPresent()) {
                Set<String> skillIdSet = skillIdSetOptional.get();
                for (String skillId : skillIdSet) {
                    commonDataOperateService.addCallTime(companyCode, agentId, skillId, timestamp, duration);
                }
            }
        }
    }

    private Optional<Set<String>> getSkillIdSetOptional(String agentId) {
        AgentWeightInfoDTO agentWithWeightInfo = commonDataOperateService.getAgentWithWeightInfo(agentId);
        return commonDataOperateService.getSkillIdFromAgentWeight(agentWithWeightInfo);
    }

    private double getCallTIme(CallUuidContext callUuidContext) {
        try {
            CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
            Long answerTimestamp = callCdrDTO.getAnswerTimestamp();
            if (Objects.isNull(answerTimestamp)) {
                return 0D;
            }
            Long hangupTimestamp = callCdrDTO.getHangupTimestamp();
            return (double) (hangupTimestamp - answerTimestamp) / 1000;
        } catch (Exception e) {
            log.error("[挂断事件] callId: {}, getCallTIme error: ", callUuidContext.getMainCallId(), e);
            return 0D;
        }
    }
}
