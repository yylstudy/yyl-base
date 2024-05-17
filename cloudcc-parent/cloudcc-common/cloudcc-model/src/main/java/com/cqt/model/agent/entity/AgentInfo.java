package com.cqt.model.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 16:25
 * 坐席基本信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@TableName("cloudcc_agent_info")
public class AgentInfo implements Serializable {

    private static final long serialVersionUID = 466167548626986232L;
    
    /**
     * 系统坐席id, 格式{companyCode}_{agentId}
     */
    @TableId(type = IdType.INPUT)
    private String sysAgentId;

    /**
     * 租户id 同企业编码
     */
    private String tenantId;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 坐席名称
     */
    private String agentName;

    /**
     * 外显号
     */
    private String displayNumber;

    /**
     * 坐席绑定分机模式 1、自动绑定 2、自定义绑定
     */
    private Integer extBindMode;

    /**
     * 坐席绑定的分机号
     */
    private String extId;

    /**
     * 坐席绑定的分机号 格式{companyCode}_{extId}
     */
    private String sysExtId;

    /**
     * 坐席状态 0：禁用 1：启用
     */
    private Integer state;

    /**
     * 分机注册方式 1、webrtc 2、其他第三方话机
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
     * 部门id
     */
    private String departIds;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据权限
     */
    private Integer dataScope;

    /**
     * 坐席应答方式 1 手动应答 2 自动应答
     */
    private Integer agentAnswerMode;

    /**
     * 语音来电应答方式 （ 1、语音应答；2、视频应答）
     */
    private Integer voiceCallAnswerMode;

    /**
     * 视频来电应答方式 （ 1、语音应答；2、视频应答）
     */
    private Integer videoCallAnswerMode;

    /**
     * 拨号盘回车键默认呼出方式 （1、语音；2、480P视频；3、720P视频）
     */
    private Integer enterKeyCallMode;

    /**
     * 视频外呼是否默认关摄像头（0：否；1：是）
     */
    private Integer videoCallTurnOffCamera;

    /**
     * 服务模式
     *
     * @since 7.0.0
     */
    private Integer serviceMode;

    private String qualityCheckAbilities;

    private String callNoteAbilities;
}
