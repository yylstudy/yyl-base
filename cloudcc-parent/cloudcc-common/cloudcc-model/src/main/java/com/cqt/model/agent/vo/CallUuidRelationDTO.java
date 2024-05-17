package com.cqt.model.agent.vo;

import com.cqt.base.enums.*;
import com.cqt.base.enums.cdr.ReleaseDirEnum;
import com.cqt.base.enums.trans.TransModeEnum;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.dto.ClientCallDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:46
 * ttl 1d
 * 通话uuid之间关联关系
 * {
 * number: 分机id或号码,
 * request_id: xxx,
 * time: xxxx,
 * uuid: xxx,
 * role: '主叫, 被叫, 监听坐席, 转接坐席, 耳语坐席..',
 * relation: [{
 * number: 分机id或号码,
 * request_id: xxx,
 * time: xxxx,
 * uuid: xxx,
 * role: '主叫, 被叫, 监听坐席, 转接坐席, 耳语坐席..'
 * }]
 * }
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallUuidRelationDTO implements Serializable {

    private static final long serialVersionUID = 5922979223276192581L;

    /**
     * 标识当前uuid为主话单
     */
    private Boolean mainCdrFlag;

    /**
     * 主话单id-自定义
     * 主叫为准
     */
    private String mainCallId;

    /**
     * 坐席还是客户号码
     * agent
     * client
     */
    private CallTypeEnum callTypeEnum;

    /**
     * 呼叫方向
     */
    private CallDirectionEnum callDirectionEnum;

    /**
     * 外呼时-主叫号码
     */
    private String callerNumber;

    /**
     * 外呼时-外显号码
     */
    private String displayNumber;

    /**
     * 外呼时-被叫号码
     */
    private String calleeNumber;

    /**
     * 平台号码
     */
    private String platformNumber;

    /**
     * 请求id
     */
    private String reqId;

    /**
     * 通话uuid的号码
     * 客户-客户号码
     * 坐席-坐席id
     */
    private String number;

    private String os;

    /**
     * 通话uuid
     */
    private String uuid;

    /**
     * 桥接的uuid
     * 当前关联的uuid
     * uuid --- relationUuid 这两个当前建立过通话
     */
    private String relationUuid;

    /**
     * 关联的主uuid,
     * 原始主叫的uuid
     */
    private String mainUuid;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 外呼客户时的外显号码
     */
    private String callBridgeDisplayNumber;

    /**
     * 坐席与客户通话的角色枚举
     *
     * @see com.cqt.base.enums.CallRoleEnum
     */
    private CallRoleEnum callRoleEnum;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 分机id
     */
    private String extId;

    /**
     * 分机ip
     */
    private String extIp;

    /**
     * fs的服务id
     */
    private String serverId;

    /**
     * 外呼之后动作
     */
    private OriginateAfterActionEnum originateAfterActionEnum;

    /**
     * 话务条播放录音文件路径
     */
    private String playRecordPath;

    /**
     * xfer动作枚举
     * 【consult:咨询，trans:转接，three_way:三方通话，whisper:耳语，eavesdrop:监听】
     */
    private XferActionEnum xferActionEnum;

    /**
     * xfer接口的uuid字段
     */
    private String xferUUID;

    /**
     * 咨询模式-盲转, 咨询转
     * 主要在坐席盲转时只挂断自己, 不挂断其他
     */
    private TransModeEnum transModeEnum;

    /**
     * 话单-记录事件信息
     */
    private CallCdrDTO callCdrDTO;

    /**
     * 外呼请求参数
     */
    private ClientCallDTO clientCallDTO;

    /**
     * 当前通话是否变成三方通话
     */
    private Boolean threeWay;

    /**
     * 语音流
     *
     * @see MediaStreamEnum
     */
    private Integer audio;

    /**
     * 视频流
     *
     * @see MediaStreamEnum
     */
    private Integer video;

    /**
     * 音视频切换标志
     */
    private Boolean changeMediaFlag;

    /**
     * 呼入坐席标识 通知SDK
     */
    private Boolean callInFlag;

    /**
     * 呼入通道
     */
    private CallInChannelEnum callInChannel;

    /**
     * 转接, 挂断原坐席通话
     */
    private String transHangupOriginCallUuid;

    /**
     * 咨询转接, 挂断原坐席通话
     */
    private Boolean transHangup;

    /**
     * 是否在挂断事件挂断全部通话
     */
    private Boolean hangupAll;

    /**
     * 签出(强签)标志 - 结束话务 转离线
     */
    private Boolean checkoutToOffline;

    /**
     * 呼入转接坐席id
     */
    private String callinTransferAgentId;

    /**
     * 外呼任务标志  client caller  predict ivr
     */
    private Boolean outCallTaskFlag;

    /**
     * 放音结束 挂断话务
     */
    private Boolean hangupAfterPlayback;

    /**
     * 挂断方
     */
    private ReleaseDirEnum releaseDir;

    /**
     * 录制节点
     */
    private RecordNodeEnum recordNode;
}
