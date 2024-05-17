package com.cqt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.entity.AgentSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-25 13:46
 */
@Mapper
public interface AgentInfoMapper extends BaseMapper<AgentInfo> {

    @Update("update cloudcc_agent_info set service_mode = #{serviceMode} where sys_agent_id = #{sysAgentId}")
    void updateServiceMode(@Param("sysAgentId") String sysAgentId, @Param("serviceMode") Integer serviceMode);

    @Select(" select sys_ext_id from cloudcc_agent_info where sys_agent_id = #{agentId} limit 1")
    String getExtIdByAgentId(String agentId);

    @Select(" select sys_agent_id from cloudcc_agent_info where sys_ext_id = #{extId} limit 1")
    String getAgentIdByExtId(String extId);

    /**
     * 查询坐席关联的技能权值映射
     *
     * @param agentId 系统级坐席id列表
     * @return List
     */
    @Select(" SELECT `t1`.`agent_id`, `t1`.`sys_agent_id`, `t2`.`skill_id`, `t2`.`agent_weight`, `t2`.`skill_weight` " +
            " FROM cloudcc_agent_info `t1` " +
            " INNER JOIN `cloudcc_agent_skill` `t2` ON `t2`.`sys_agent_id` = `t1`.`sys_agent_id` " +
            " AND `t1`.`sys_agent_id` = #{agentId} ")
    List<AgentSkill> findAgentSkillWeights(String agentId);

    /**
     * 查询坐席关联的技能包下包含的技能权值映射
     *
     * @param agentId 系统级坐席id列表
     * @return List
     */
    @Select(" SELECT `t1`.`agent_id`, `t1`.`sys_agent_id`, `t3`.`skill_id`, `t2`.`agent_weight`, `t2`.`skill_weight` " +
            " FROM cloudcc_agent_info `t1` " +
            " LEFT JOIN `cloudcc_agent_skill_package` `t2` ON `t2`.`sys_agent_id` = `t1`.`sys_agent_id` " +
            " INNER JOIN `cloudcc_skill_pack_re_skill` `t3` ON `t3`.`skill_pack_id`=`t2`.`skill_pack_id` " +
            " AND `t1`.`sys_agent_id` = #{agentId} ")
    List<AgentSkill> findAgentSkillPackContainSkillWeighs(String agentId);
}
