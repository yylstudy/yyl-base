package com.cqt.model.agent.vo;

import com.cqt.model.agent.entity.AgentInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * date 2023-07-12 11:26:11:26
 */
@Data
@ApiModel(value = "坐席信息参数", description = "坐席信息参数")
public class AgentInfoVO implements Serializable {

    private static final long serialVersionUID = 4947947821262572985L;

    /**
     * 坐席工号
     */
    @ApiModelProperty(value = "坐席工号", required = true)
    private String sysAgentId;

    /**
     * 分机注册方式 1、webrtc 2、第三方话机
     */
    @ApiModelProperty(value = "分机注册方式 1、webrtc 2、第三方话机")
    private Integer extRegMode;

    /**
     * 手机接听离线坐席（0：关闭 1：开启）
     */
    @ApiModelProperty(value = "手机接听离线坐席（0：关闭 1：开启）")
    private Integer offlineAgent;

    /**
     * 离线坐席接续的手机
     */
    @ApiModelProperty(value = "离线坐席接续的手机")
    private String phoneNumber;

    /**
     * 坐席应答方式 1 手动应答 2 自动应答
     */
    @ApiModelProperty(value = "坐席应答方式 1 手动应答 2 自动应答")
    private Integer agentAnswerMode;

    /**
     * 语音来电应答方式 （ 1、语音应答；2、视频应答）
     */
    @ApiModelProperty(value = "语音来电应答方式 （ 1、语音应答；2、视频应答）")
    private Integer voiceCallAnswerMode;

    /**
     * 视频来电应答方式 （ 1、语音应答；2、视频应答）
     */
    @ApiModelProperty(value = "视频来电应答方式 （ 1、语音应答；2、视频应答）")
    private Integer videoCallAnswerMode;

    /**
     * 拨号盘回车键默认呼出方式 （1、语音；2、480P视频；3、720P视频）
     */
    @ApiModelProperty(value = "拨号盘回车键默认呼出方式 （1、语音；2、480P视频；3、720P视频）")
    private Integer enterKeyCallMode;

    /**
     * 视频外呼是否默认关摄像头（0：否；1：是）
     */
    @ApiModelProperty(value = "视频外呼是否默认关摄像头（0：否；1：是）")
    private Integer videoCallTurnOffCamera;

    /**
     * 签入后默认服务模式
     */
    @ApiModelProperty(value = "签入后默认服务模式（1：客服型，2：外呼型）")
    private Integer serviceMode;

    private String qualityCheckAbilities;

    private String callNoteAbilities;

    public static AgentInfoVO convert(AgentInfo agentInfo) {
        AgentInfoVO agentInfoVO = new AgentInfoVO();
        agentInfoVO.setSysAgentId(agentInfo.getSysAgentId());
        agentInfoVO.setExtRegMode(agentInfo.getExtRegMode());
        agentInfoVO.setOfflineAgent(agentInfo.getOfflineAgent());
        agentInfoVO.setPhoneNumber(agentInfo.getPhoneNumber());
        agentInfoVO.setAgentAnswerMode(agentInfo.getAgentAnswerMode());
        agentInfoVO.setEnterKeyCallMode(agentInfo.getEnterKeyCallMode());
        agentInfoVO.setVoiceCallAnswerMode(agentInfo.getVoiceCallAnswerMode());
        agentInfoVO.setVideoCallAnswerMode(agentInfo.getVideoCallAnswerMode());
        agentInfoVO.setVideoCallTurnOffCamera(agentInfo.getVideoCallTurnOffCamera());
        agentInfoVO.setServiceMode(agentInfo.getServiceMode());
        agentInfoVO.setQualityCheckAbilities(agentInfo.getQualityCheckAbilities());
        agentInfoVO.setCallNoteAbilities(agentInfo.getCallNoteAbilities());
        return agentInfoVO;
    }
}
