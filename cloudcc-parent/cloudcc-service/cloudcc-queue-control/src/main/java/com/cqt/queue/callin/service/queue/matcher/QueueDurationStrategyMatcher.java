package com.cqt.queue.callin.service.queue.matcher;

import cn.hutool.core.collection.CollUtil;
import com.cqt.base.enums.QueueStrategyEnum;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.queue.CallQueueContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-13 15:32
 * 排队时长最大的优先
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueDurationStrategyMatcher implements ElementMatcher<CallQueueContext> {

    private final DataStoreService dataStoreService;

    @Override
    public boolean matches(CallQueueContext context) {
        try {
            queue(context);
        } catch (Exception e) {
            log.error("[QueueDuration] execute error: ", e);
        }
        return true;
    }

    private void queue(CallQueueContext context) throws Exception {
        String companyCode = context.getCompanyCode();
        // 先进先出策略的客户
        List<UserQueueUpDTO> userQueueUpList = context.getQueueStrategyMap().get(QueueStrategyEnum.TIME.getCode());
        if (CollUtil.isEmpty(userQueueUpList)) {
            return;
        }
        List<TransferAgentQueueDTO> freeAgents = context.getFreeAgents();
        if (CollUtil.isEmpty(freeAgents)) {
            return;
        }
        List<UserQueueUpDTO> fifoList = userQueueUpList.stream()
                .sorted(Comparator.comparing(UserQueueUpDTO::getCurrentTimestamp))
                .collect(Collectors.toList());
        for (UserQueueUpDTO userQueueUpDTO : fifoList) {
            if (CollUtil.isEmpty(freeAgents)) {
                return;
            }
            String callerNumber = userQueueUpDTO.getCallerNumber();
            Iterator<TransferAgentQueueDTO> iterator = freeAgents.iterator();
            while (iterator.hasNext()) {
                TransferAgentQueueDTO transferAgentQueueDTO = iterator.next();
                String agentId = transferAgentQueueDTO.getAgentId();
                FreeswitchApiVO freeswitchApiVO = dataStoreService.callBridgeAgent(userQueueUpDTO, agentId, "");
                if (freeswitchApiVO.getResult()) {
                    // 外呼成功再移除, 将企业空闲坐席队列移除该坐席id
                    boolean remove = dataStoreService.deleteFreeAgentQueue(companyCode, agentId);
                    log.info("[排队-客户最高等级] 分配坐席成功, 外呼并桥接成功, 企业: {}, 坐席: {}, 移除空闲队列: {}",
                            companyCode, agentId, remove);
                    iterator.remove();
                    context.removeQueueUser(userQueueUpDTO);
                    boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
                    log.info("[排队-客户最高等级] 分配坐席成功, 企业: {}, 来电号码: {}, 移除排队: {}",
                            companyCode, callerNumber, removed);
                }
            }
        }
    }
}
