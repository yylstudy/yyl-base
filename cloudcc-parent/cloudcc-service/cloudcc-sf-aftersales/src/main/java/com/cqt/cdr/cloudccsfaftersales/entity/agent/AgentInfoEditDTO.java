package com.cqt.cdr.cloudccsfaftersales.entity.agent;


import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Xienx
 * @date 2023-07-12 11:26:11:26
 */
@Data
public class AgentInfoEditDTO implements Serializable {

    private static final long serialVersionUID = -627416233605828994L;

    /**
     * 坐席工号
     */
    @NotBlank(message = "坐席工号不能为空")
    private String agentId;

    /**
     * 坐席姓名
     */
    @NotBlank(message = "坐席姓名不能为空")
    private String agentName;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 角色（菜单访问权限）
     */
    @NotEmpty(message = "角色id不能为空")
    private List<String> roleIdList;

    /**
     * 班组
     */
    private List<String> departIdList;

    /**
     * 数据权限
     */
    private Integer dataScope;

    /**
     * 外显号
     */
    private String displayNumber;

    /**
     * 状态 0:禁用 1:启用
     */
    @NotNull(message = "坐席状态不能为空")
    private Integer state;

    /**
     * 技能权值配置
     */
    private List<SkillWeightInfo> skillWeightInfos;

    /**
     * 坐席绑定分机模式
     */
    @NotNull(message = "坐席绑定分机模式不能为空")
    private Integer extBindMode;

    /**
     * 自定义绑定的分机号
     */
    private String extId;

    /**
     * 分机注册方式 1、webrtc 2、第三方话机
     */
    private Integer extRegMode;

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
    @Min(value = 0, message = "事后处理时间应为非负数")
    private Integer processTime;

    /**
     * 手机接听离线坐席（0：关闭 1：开启）
     */
    private Integer offlineAgent;

    /**
     * 离线坐席接续的手机
     */
    private String phoneNumber;

}
