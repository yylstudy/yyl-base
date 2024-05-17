package com.cqt.queue.callin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.queue.callin.cache.UserQueueCache;
import com.cqt.queue.callin.service.DataQueryService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-18 18:09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryServiceImpl implements DataQueryService {

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public List<UserQueueUpDTO> getUserQueueUpList(String companyCode) {
        // String userQueueLevelKey = CacheUtil.getUserQueueLevelKey(companyCode);
        // query local cache
        List<UserQueueUpDTO> queue = UserQueueCache.get(companyCode);
        if (CollUtil.isEmpty(queue)) {
            return Lists.newArrayList();
        }
        /*Map<String, String> map = redissonUtil.readAllHash(userQueueLevelKey);
        if (CollUtil.isEmpty(map)) {
            return Lists.newArrayList();
        }
        List<UserQueueUpDTO> userQueueUpDtoList = new ArrayList<>();
        for (String value : map.values()) {
            if (StrUtil.isEmpty(value)) {
                continue;
            }
            try {
                userQueueUpDtoList.add(objectMapper.readValue(value, UserQueueUpDTO.class));
            } catch (JsonProcessingException e) {
                log.error("[getUserQueueUpList] companyCode: {}, error: ", companyCode, e);
            }
        }*/
        return queue;
    }

    @Override
    public Boolean checkAgentAndExtStatus(String companyCode, String agentId) {
        // 判断坐席是否在线且空闲
        Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
        if (agentStatusOptional.isPresent()) {
            AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
            return AgentStatusEnum.FREE.name().equals(agentStatusDTO.getTargetStatus());
        }
        // 绑定分机是否在线可用
        return false;
    }

}
