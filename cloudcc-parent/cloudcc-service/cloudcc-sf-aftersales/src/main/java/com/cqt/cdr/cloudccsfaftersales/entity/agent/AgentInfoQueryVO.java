package com.cqt.cdr.cloudccsfaftersales.entity.agent;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Xienx
 * @date 2023-07-11 15:34:15:34
 */
@Data
public class AgentInfoQueryVO implements Serializable {

    private static final long serialVersionUID = -6191363161774262475L;

    /**
     * 坐席工号
     */
    private String agentId;

    /**
     * 系统级坐席工号
     */
    private String sysAgentId;

    /**
     * 绑定分机号
     */
    private String extId;

    /**
     * 坐席名称
     */
    private String agentName;

    /**
     * 分机注册方式
     */
    private Integer extRegMode;

    /**
     * 角色（菜单访问权限）
     */
    private List<String> roleIdList;


    /**
     * 角色（菜单访问权限）
     */
    private List<String> roleNameList;

    /**
     * 班组id
     */
    private List<String> departIdList;

    /**
     * 班组名称
     */
    private List<String> departNameList;

    /**
     * 技能组id
     */
    private List<String> skillPackIdList;

    /**
     * 技能组名称
     */
    private List<String> skillPackNameList;

    /**
     * 技能id
     */
    private List<String> skillIdList;

    /**
     * 技能名称
     */
    private List<String> skillNameList;

    /**
     * 外显号
     */
    private String displayNumber;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 坐席绑定分机模式 1、自动绑定 2、自定义绑定
     */
    private Integer extBindMode;

    /**
     * 自动示忙 0：关闭 1：开启
     * 话务分配给坐席，坐席设置自动应答且应答失败，或坐席设置手动应答且拒接来电，坐席状态是否自动变更为示忙
     */
    private Integer autoShowBusy;

    /**
     * 事后处理 0：关闭 1：开启
     */
    private Integer postProcess;

    /**
     * 事后处理时间 秒
     */
    private Integer processTime;

    /**
     * 手机接听离线坐席（0：关闭 1：开启）
     */
    private Integer offlineAgent;

    /**
     * 离线坐席接续的手机
     */
    private String phoneNumber;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据权限
     */
    private Integer dataScope;

    /**
     * 导出时需要设置的角色名称字段
     */
    public String convertgetRoleNameList() {
        return StrUtil.join("、", roleNameList);
    }

    /**
     * 导出时需要设置的班组名称字段
     */
    public String convertgetDepartNameList() {
        return StrUtil.join("、", departNameList);
    }

    /**
     * 导出时需要设置的技能组名称字段
     */
    public String convertgetSkillPackNameList() {
        return StrUtil.join("、", skillPackNameList);
    }

    /**
     * 导出时需要设置的技能名称字段
     */
    public String convertgetSkillNameList() {
        return StrUtil.join("、", skillNameList);
    }
}
