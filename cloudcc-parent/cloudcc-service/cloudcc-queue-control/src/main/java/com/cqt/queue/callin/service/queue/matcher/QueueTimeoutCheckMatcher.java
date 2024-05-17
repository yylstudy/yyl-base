package com.cqt.queue.callin.service.queue.matcher;

import cn.hutool.core.date.DateUtil;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.freeswitch.dto.api.StopPlayDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.queue.CallQueueContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-13 15:32
 * 排队超时检测
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueTimeoutCheckMatcher implements ElementMatcher<CallQueueContext> {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    @Override
    public boolean matches(CallQueueContext context) {
        List<UserQueueUpDTO> userQueueUpList = context.getUserQueueUpList();
        checkQueueUpIsTimeout(userQueueUpList);
        Map<Integer, List<UserQueueUpDTO>> queueStrategyMap = userQueueUpList.stream()
                .collect(Collectors.groupingBy(UserQueueUpDTO::getQueueStrategy));
        context.setQueueStrategyMap(queueStrategyMap);
        return true;
    }

    /**
     * 判断排队是否超时
     *
     * @param list 排队人员
     */
    private void checkQueueUpIsTimeout(List<UserQueueUpDTO> list) {
        Iterator<UserQueueUpDTO> iterator = list.iterator();
        while (iterator.hasNext()) {
            UserQueueUpDTO userQueueUpDTO = iterator.next();
            String companyCode = userQueueUpDTO.getCompanyCode();
            String uuid = userQueueUpDTO.getUuid();
            String callerNumber = userQueueUpDTO.getCallerNumber();

            // 是否已挂断
            Boolean hangup = commonDataOperateService.isHangup(companyCode, uuid);
            if (Boolean.TRUE.equals(hangup)) {
                boolean removed = isRemoved(userQueueUpDTO, iterator);
                log.info("[排队-超时检测] 企业: {}, 来电号码: {}, 已挂断, 移除排队: {}", companyCode, callerNumber, removed);
                continue;
            }

            // 开始排队时的时间戳-调用转技能接口时
            Long queueTimestamp = userQueueUpDTO.getCurrentTimestamp();
            // 排队超时时间
            Integer maxQueueTime = userQueueUpDTO.getMaxQueueTime();

            // 当前时间
            long currentTimeMillis = System.currentTimeMillis();
            long duration = (currentTimeMillis - queueTimestamp) / 1000L;

            String queueTime = DateUtil.formatDateTime(DateUtil.date(queueTimestamp));
            log.info("[排队-超时检测] 企业: {}, 来电号码: {}, uuid: {}, startQueue: {}, max: {}, diff: {}",
                    companyCode, callerNumber, uuid, queueTime, maxQueueTime, duration);

            if (duration > maxQueueTime) {
                // 结束放音
                stopPlayWaitTone(userQueueUpDTO);
                dataStoreService.callIvrExit(userQueueUpDTO);
                boolean removed = isRemoved(userQueueUpDTO, iterator);
                log.info("[排队-超时检测] 企业: {}, uuid: {}, 号码: {}, 排队时间: {}, 排队阈值: {}, 已超时, 结束排队: {}",
                        companyCode, uuid, callerNumber, duration, maxQueueTime, removed);
            }
        }
    }

    private boolean isRemoved(UserQueueUpDTO userQueueUpDTO, Iterator<UserQueueUpDTO> iterator) {
        boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
        iterator.remove();
        return removed;
    }

    /**
     * 结束放音
     *
     * @param userQueueUpDTO 排队信息
     */
    private void stopPlayWaitTone(UserQueueUpDTO userQueueUpDTO) {
        StopPlayDTO stopPlayDTO = StopPlayDTO.build(userQueueUpDTO.getCompanyCode(), userQueueUpDTO.getUuid());
        freeswitchRequestService.stopPlay(stopPlayDTO);
    }
}
