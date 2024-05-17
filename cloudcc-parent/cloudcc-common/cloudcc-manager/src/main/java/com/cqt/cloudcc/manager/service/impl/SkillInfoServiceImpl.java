package com.cqt.cloudcc.manager.service.impl;

import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.SkillInfoService;
import com.cqt.mapper.SkillInfoMapper;
import com.cqt.model.skill.entity.SkillInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-08-30 11:21
 * 技能
 * 人工服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillInfoServiceImpl implements SkillInfoService {

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final SkillInfoMapper skillInfoMapper;

    @Override
    public SkillInfo getSkillInfo(String skillId) {
        String skillInfoKey = CacheUtil.getSkillInfoKey(skillId);
        try {
            SkillInfo skillInfo = redissonUtil.get(skillInfoKey, SkillInfo.class);
            if (Objects.nonNull(skillInfo)) {
                return skillInfo;
            }
        } catch (Exception e) {
            log.error("[查询技能信息] key: {}, 异常: ", skillInfoKey, e);
        }
        SkillInfo skillInfo = skillInfoMapper.selectById(skillId);
        if (Objects.nonNull(skillInfo)) {
            try {
                redissonUtil.set(skillInfoKey, objectMapper.writeValueAsString(skillInfo));
            } catch (Exception e) {
                log.error("[技能信息-回写redis] key: {}, 异常: ", skillInfoKey, e);
            }
        }
        return skillInfo;
    }

}
