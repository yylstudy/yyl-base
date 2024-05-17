package com.cqt.queue.callin.service.queue.matcher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.queue.CallQueueContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-10-13 15:32
 * 离线坐席, 呼叫配置手机号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfflinePhoneMatcher implements ElementMatcher<CallQueueContext> {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public boolean matches(CallQueueContext context) {
        try {
            offline(context);
        } catch (Exception e) {
            log.error("[OfflinePhone] execute error: ", e);
        }
        return true;
    }

    private void offline(CallQueueContext context) throws Exception {
        String companyCode = context.getCompanyCode();
        List<UserQueueUpDTO> userQueueUpList = context.getUserQueueUpList();
        if (CollUtil.isEmpty(userQueueUpList)) {
            return;
        }

        List<TransferAgentQueueDTO> offlineAgents = commonDataOperateService.getCompanyAgentQueue(companyCode,
                AgentServiceModeEnum.CUSTOMER, false);
        if (CollUtil.isEmpty(offlineAgents)) {
            return;
        }

        Iterator<UserQueueUpDTO> iterator = userQueueUpList.iterator();
        while (iterator.hasNext()) {
            UserQueueUpDTO userQueueUpDTO = iterator.next();
            String callerNumber = userQueueUpDTO.getCallerNumber();
            String skillId = userQueueUpDTO.getSkillId();
            // 查询离线坐席-配置手机号
            MatchAgentWeightVO matchOfflineAgentWeightVO = dataStoreService.matchAgentWeightBySkillId(offlineAgents,
                    companyCode, skillId, false);
            String matchPhoneNumber = matchOfflineAgentWeightVO.getMatchPhoneNumber();
            if (matchOfflineAgentWeightVO.getExistMatchAgent() && StrUtil.isNotEmpty(matchPhoneNumber)) {
                String matchAgentId = matchOfflineAgentWeightVO.getMatchAgentId();
                FreeswitchApiVO freeswitchApiVO = dataStoreService.callBridgeAgent(userQueueUpDTO, matchAgentId,
                        matchPhoneNumber);
                if (freeswitchApiVO.getResult()) {
                    // 外呼成功再移除, 将企业空闲坐席队列移除该坐席id
                    boolean remove = dataStoreService.deleteFreeAgentQueue(companyCode, matchAgentId);
                    log.info("[排队-离线坐席] 分配坐席成功, 外呼并桥接成功, 企业: {}, 坐席: {}, 手机号: {}, 移除空闲队列: {}",
                            companyCode, matchAgentId, matchPhoneNumber, remove);
                    iterator.remove();
                    boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
                    log.info("[排队-离线坐席] 分配坐席成功, 企业: {}, 来电号码: {}, 手机号: {}, 移除排队: {}",
                            companyCode, callerNumber, matchPhoneNumber, removed);
                }
            }
        }
    }
}
