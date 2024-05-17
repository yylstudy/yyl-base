package com.cqt.queue.callin.service.queue.matcher;

import cn.hutool.core.collection.CollUtil;
import com.cqt.base.enums.QueueStrategyEnum;
import com.cqt.base.util.MapUtils;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;
import com.cqt.queue.callin.service.DataQueryService;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.queue.CallQueueContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-13 15:32
 * 组合策略(客户优先级, 坐席技能权值, 先进先出)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CombineStrategyMatcher implements ElementMatcher<CallQueueContext> {

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    private final ObjectMapper objectMapper;

    @Override
    public boolean matches(CallQueueContext context) {
        try {
            combine(context);
        } catch (Exception e) {
            log.error("[CombineStrategy] execute error: ", e);
        }
        return true;
    }

    private void combine(CallQueueContext context) throws Exception {
        String companyCode = context.getCompanyCode();
        List<TransferAgentQueueDTO> freeAgents = context.getFreeAgents();
        if (CollUtil.isEmpty(freeAgents)) {
            return;
        }

        // 组合策略的客户
        List<UserQueueUpDTO> userQueueUpList = context.getQueueStrategyMap().get(QueueStrategyEnum.COMBINE.getCode());
        if (CollUtil.isEmpty(userQueueUpList)) {
            return;
        }
        initLevel(userQueueUpList);
        Map<Integer, List<UserQueueUpDTO>> levelGroup = userQueueUpList.stream()
                .collect(Collectors.groupingBy(UserQueueUpDTO::getLevel));
        TreeMap<Integer, List<UserQueueUpDTO>> treeLevelGroup = new TreeMap<>(levelGroup);
        for (Map.Entry<Integer, List<UserQueueUpDTO>> entry : treeLevelGroup.entrySet()) {
            Integer level = entry.getKey();
            List<UserQueueUpDTO> list = entry.getValue();

            if (list.size() == 1) {
                // 3.4 判断第一个等级的list大小是否为1, 是则当前客户是等级最大的, 优先取匹配技能的坐席, 取出坐席权值最小的坐席,
                // 坐席权值一样的取最早空闲的坐席
                UserQueueUpDTO userQueueUpDTO = list.get(0);
                String userSkillId = userQueueUpDTO.getSkillId();
                String callerNumber = userQueueUpDTO.getCallerNumber();
                if (log.isInfoEnabled()) {
                    log.info("[排队-客户最高等级] 企业id: {}, 来电号码: {}, 等级: {}, 排队信息: {}",
                            companyCode, callerNumber, level, objectMapper.writeValueAsString(userQueueUpDTO));
                }

                // 查与客户的技能id匹配的坐席
                MatchAgentWeightVO matchAgentWeightVO = dataStoreService.matchAgentWeightBySkillId(freeAgents,
                        companyCode, userSkillId, true);
                // 看是否有坐席可用
                if (matchAgentWeightVO.getExistMatchAgent()) {
                    String matchAgentId = matchAgentWeightVO.getMatchAgentId();
                    FreeswitchApiVO freeswitchApiVO = dataStoreService.callBridgeAgent(userQueueUpDTO, matchAgentId, "");
                    if (freeswitchApiVO.getResult()) {
                        // 外呼成功再移除, 将企业空闲坐席队列移除该坐席id
                        boolean remove = dataStoreService.deleteFreeAgentQueue(companyCode, matchAgentId);
                        log.info("[排队-客户最高等级] 分配坐席成功, 外呼并桥接成功, 企业: {}, 坐席: {}, 来电号码: {}, 移除空闲队列: {}",
                                companyCode, matchAgentId, callerNumber, remove);
                        context.removeQueueUser(userQueueUpDTO);
                        boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
                        log.info("[排队-客户最高等级] 分配坐席成功, 企业: {}, 来电号码: {}, 移除排队: {}",
                                companyCode, callerNumber, removed);
                    }
                    list.remove(userQueueUpDTO);
                    if (log.isInfoEnabled()) {
                        log.info("[排队-客户最高等级] 企业id: {}, 来电号码: {}, 等级: {}, 分配坐席: {} 发起外呼并桥接.",
                                companyCode, callerNumber, level, matchAgentId);
                    }
                    continue;
                }
                log.info("[排队-客户最高等级] 企业id: {}, 来电号码: {}, 等级: {}, 技能id: {}, 无匹配的坐席, 继续下一个等级",
                        companyCode, callerNumber, level, userSkillId);
            }

            // 5. 等级最高存在多个客户, list大小 > 1
            if (log.isInfoEnabled()) {
                log.info("[排队-客户同等级] 企业id: {}, 客户等级: {}, 当前排队人员信息: {}",
                        companyCode, level, objectMapper.writeValueAsString(list));
            }
            Iterator<TransferAgentQueueDTO> iterator = freeAgents.iterator();
            while (iterator.hasNext()) {
                TransferAgentQueueDTO transferAgentQueueDTO = iterator.next();
                String agentId = transferAgentQueueDTO.getAgentId();
                log.info("[排队] start, freeAgents: {}, 坐席: {}", freeAgents.size(), agentId);

                // 检测坐席状态是否空闲
                Boolean isFree = dataQueryService.checkAgentAndExtStatus(companyCode, agentId);
                if (!isFree) {
                    log.info("[排队-客户同等级] 企业: {}, 坐席: {}, 坐席非空闲!", companyCode, agentId);
                    iterator.remove();
                    continue;
                }

                AgentWeightInfoDTO agentWeightInfoDTO = transferAgentQueueDTO.getAgentWeightInfoDTO();
                Map<String, Integer> skillWeightMap = agentWeightInfoDTO.getSkill();
                if (CollUtil.isEmpty(skillWeightMap)) {
                    log.info("[排队-客户同等级] 企业id: {}, 坐席id: {}, 未配置技能权值!", companyCode, agentId);
                    continue;
                }
                // 坐席的技能权值map 根据技能权值分组
                Map<Integer, List<String>> groupByValue = MapUtils.groupByValue(skillWeightMap.entrySet(), true);
                if (log.isInfoEnabled()) {
                    log.info("[排队-客户同等级] 企业id: {}, 坐席id: {}, 技能权值配置: {}",
                            companyCode, agentId, objectMapper.writeValueAsString(groupByValue));
                }
                for (Map.Entry<Integer, List<String>> listEntry : groupByValue.entrySet()) {
                    Integer skillWeight = listEntry.getKey();
                    List<String> skillIdList = listEntry.getValue();
                    // 查用户的技能是否有匹配skillIdList其中一个的
                    List<UserQueueUpDTO> matchSkillIdList = list.stream()
                            .filter(e -> skillIdList.contains(e.getSkillId()))
                            .collect(Collectors.toList());
                    if (log.isInfoEnabled()) {
                        log.info("[排队-客户同等级] 开始判断 企业: {}, 预期坐席: {}, 技能权值: {}, 技能: {}, 技能匹配的排队人员: {}",
                                companyCode, agentId, skillWeight, skillIdList,
                                objectMapper.writeValueAsString(matchSkillIdList));
                    }
                    // 没有一致的. 继续下一个技能权值
                    if (CollUtil.isEmpty(matchSkillIdList)) {
                        continue;
                    }
                    // 只有一个客户一样, 直接分配
                    if (matchSkillIdList.size() == 1) {
                        UserQueueUpDTO userQueueUpDTO = matchSkillIdList.get(0);
                        // 找到了 agentId
                        FreeswitchApiVO freeswitchApiVO = dataStoreService.callBridgeAgent(userQueueUpDTO, agentId, "");
                        if (freeswitchApiVO.getResult()) {
                            boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
                            list.remove(userQueueUpDTO);
                            iterator.remove();
                            log.info("[排队-客户同等级] 技能匹配的排队人员只有一个, 企业: {}, 确定分配坐席id: {}, 移除排队: {}, 客户信息: {}, 剩余空闲坐席: {}",
                                    companyCode, agentId, removed, objectMapper.writeValueAsString(userQueueUpDTO),
                                    freeAgents.size());
                            continue;
                        }
                    }
                    // 有多个客户一样, 根据客户的第一次排队时间, 谁先排队谁先分配
                    Optional<UserQueueUpDTO> firstQueueOptional = matchSkillIdList.stream()
                            .min(Comparator.comparing(UserQueueUpDTO::getFirstTimestamp));
                    if (firstQueueOptional.isPresent()) {
                        UserQueueUpDTO userQueueUpDTO = firstQueueOptional.get();
                        FreeswitchApiVO freeswitchApiVO = dataStoreService.callBridgeAgent(userQueueUpDTO, agentId, "");
                        if (freeswitchApiVO.getResult()) {
                            list.remove(userQueueUpDTO);
                            iterator.remove();
                            boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
                            log.info("[排队-客户同等级] 技能ids匹配的排队人员有多个,取最早排队, 企业id: {}, 确定分配坐席id: {},移除排队: {}, 客户信息: {}, 剩余空闲坐席: {}",
                                    companyCode, agentId, removed, objectMapper.writeValueAsString(userQueueUpDTO),
                                    freeAgents.size());
                        }
                    }
                }
            }
        }
    }

    private void initLevel(List<UserQueueUpDTO> userQueueUpList) {
        for (UserQueueUpDTO userQueueUpDTO : userQueueUpList) {
            if (Objects.isNull(userQueueUpDTO.getLevel())) {
                userQueueUpDTO.setLevel(Integer.MAX_VALUE);
            }
        }
    }
}
