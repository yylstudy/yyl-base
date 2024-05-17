package com.cqt.cloudcc.manager.service;

import com.cqt.model.skill.entity.SkillInfo;

/**
 * @author linshiqiang
 * date:  2023-08-30 11:21
 */
public interface SkillInfoService {

    /**
     * 查询技能配置信息
     *
     * @param skillId 技能id
     * @return 技能配置信息
     */
    SkillInfo getSkillInfo(String skillId);

}
