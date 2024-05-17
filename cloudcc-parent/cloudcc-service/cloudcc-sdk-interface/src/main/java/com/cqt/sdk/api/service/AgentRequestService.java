package com.cqt.sdk.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cqt.model.agent.vo.SkillAgentVO;
import com.cqt.sdk.api.vo.QueueAgentInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-11-22 19:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentRequestService {

    private final RedissonUtil redisUtil;

    public List<SkillAgentVO> getAgentList(String companyCode, Integer pageSize, Integer pageNo,
                                           String sysAgentId, String skillId, String keyword) {
        try {
            log.debug("话务条【请求坐席及状态列表】，查询企业号：{}", companyCode);
            Map<String, String> agentSkillMap = redisUtil.getStringMap(companyCode + ":agent_skill_relation");
            Map<String, String> relations = redisUtil.getStringMap(companyCode + ":agent_skill_relationship");
            Map<String, String> agentMap = redisUtil.getStringMap(companyCode + ":agent_status_result");
            Map<String, String> allSkillMap = redisUtil.getStringMap(companyCode + ":all:skill");
            Map<String, String> allAgentMap = redisUtil.getStringMap(companyCode + ":all:agent");
            HashMap<String, String> copy = new HashMap<>(allAgentMap);
            List<SkillAgentVO> result = new ArrayList<>();
            allSkillMap.put("无技能", "无技能");
            Set<String> strings = allAgentMap.keySet();
            strings.removeAll(relations.keySet());
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("agentIds", strings);
            jsonObject1.put("id", "无技能");
            agentSkillMap.put("无技能", jsonObject1.toJSONString());
            for (String skillIdInfo : allSkillMap.keySet()) {
                if (StringUtils.isNotBlank(skillId)) {
                    if (!skillIdInfo.equals(skillId)) {
                        continue;
                    }
                }
                SkillAgentVO skillAgentVO = new SkillAgentVO();
                skillAgentVO.setSkillId(skillIdInfo);
                skillAgentVO.setSkillName(allSkillMap.get(skillIdInfo));
                String s = agentSkillMap.get(skillAgentVO.getSkillName());
                JSONObject jsonObject = JSONObject.parseObject(s);
                JSONArray agentIds = jsonObject.getJSONArray("agentIds");
                List<SkillAgentVO.AgentListVO> agentListVoS = new ArrayList<>();
                for (Object agentId : agentIds) {
                    SkillAgentVO.AgentListVO agentListVO = new SkillAgentVO.AgentListVO();
                    if (agentId == null) {
                        continue;
                    }
                    String[] s1 = agentId.toString().split("_");
                    if (s1.length <= 1) {
                        continue;
                    }
                    String agent = s1[1];
                    agentListVO.setSysAgentId(agent);
                    if (sysAgentId.equals(agentId.toString())) {
                        continue;
                    }
                    String json = agentMap.get(agentId.toString());
                    String name = copy.get(agentId.toString());
                    QueueAgentInfo queueAgentInfo = JSONObject.parseObject(json, QueueAgentInfo.class);
                    if (queueAgentInfo == null) {
                        agentListVO.setAgentStatus("OFFLINE");
                        agentListVO.setAgentName(name);
                        if (StringUtils.isNotBlank(keyword)) {
                            if (agent.contains(keyword) || (StringUtils.isNotBlank(name) && name.contains(keyword))) {
                                agentListVoS.add(agentListVO);
                            }
                        } else {
                            agentListVoS.add(agentListVO);
                        }
                        continue;
                    }
                    agentListVO.setAgentName(queueAgentInfo.getAgentName());
                    String agentStatus = queueAgentInfo.getAgentStatus();
                    agentListVO.setAgentStatus(agentStatus);
                    if (StringUtils.isNotBlank(keyword)) {
                        if (agent.contains(keyword) || (StringUtils.isNotBlank(name) && name.contains(keyword))) {
                            agentListVoS.add(agentListVO);
                        }
                    } else {
                        agentListVoS.add(agentListVO);
                    }
                }
                int totalSize = agentListVoS.size();
                int fromIndex = pageSize * pageNo - pageSize;
                int toIndex = pageSize * pageNo;
                agentListVoS = agentListVoS.stream().sorted((o1, o2) -> {
                    String agentStatus = o1.getAgentStatus();
                    String agentStatus1 = o2.getAgentStatus();
                    if (agentStatus1.equals(agentStatus)) {
                        return 0;
                    } else if ("OFFLINE".equals(agentStatus)) {
                        return 1;
                    } else if ("FREE".equals(agentStatus)) {
                        return -1;
                    } else if ("OFFLINE".equals(agentStatus1)) {
                        return -1;
                    } else if ("FREE".equals(agentStatus1)) {
                        return 1;
                    }
                    return 0;
                }).collect(Collectors.toList());
                if (agentListVoS.size() >= toIndex) {
                    agentListVoS = agentListVoS.subList(fromIndex, toIndex);
                } else {
                    agentListVoS = agentListVoS.subList(fromIndex, agentListVoS.size());
                }

                SkillAgentVO.AgentData agentData = new SkillAgentVO.AgentData();
                agentData.setList(agentListVoS);
                agentData.setTotalCount((long) totalSize);
                agentData.setTotalPage((long) Math.ceil(totalSize * 1.0 / pageSize));
                agentData.setCurrentPage(pageNo);
                skillAgentVO.setAgents(agentData);
                result.add(skillAgentVO);
            }
            log.debug("话务条【请求坐席及状态列表】，查询企业号：{},查询结果：{}", companyCode, result);
            return result.stream().sorted(Comparator.comparing(SkillAgentVO::getSkillId)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("话务条查询坐席列表失败", e);
            return null;
        }
    }

}
