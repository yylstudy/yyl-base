package com.cqt.model.agent.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * @author linshiqiang
 * date:  2023-07-07 17:58
 * 通话uuid上下文
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallUuidContext implements Serializable {

    private static final long serialVersionUID = 7873529931871358184L;

    /**
     * 扩展字段
     */
    private Extra extra;

    /**
     * 外呼任务参数
     */
    private ClientOutboundCallTaskDTO clientOutboundCallTaskDTO;

    /**
     * 预览外呼参数
     */
    private ClientPreviewOutCallDTO clientPreviewOutCallDTO;

    /**
     * 当前uuid的信息
     */
    private CallUuidRelationDTO current;

    /**
     * 用户排队信息
     */
    private UserQueueUpDTO userQueueUpDTO;

    /**
     * 在坐席挂断事件, 判断坐席是否有桥接事件-当userQueueUpDTO不为空时
     */
    private Boolean checkAgentBridgeStatus;

    /**
     * 通话关联的uuid
     */
    private RelateUuidDTO relateUuidDTO = new RelateUuidDTO();

    /**
     * 当前uuid关联的uuid列表
     */
    private Set<String> relationUuid = new HashSet<>(8);

    /**
     * 只在主叫侧
     * 子话单 AB路对应关系
     * 在收到挂断事件之后更新value, 确定成功建立通话
     * <pre>
     * key: {a-uuid}@{b-uuid}
     * value: 是否成功建立通话 true@true
     * </pre>
     */
    private Map<String, String> cdrLink;

    /**
     * 是否为呼入ivr
     */
    private Boolean callinIVR;

    /**
     * 呼入ivr 开启留言标志
     */
    private Boolean voiceMailFlag;

    /**
     * 开始留言时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date voiceMailStartTime;

    /**
     * 结束语音信箱时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date voiceMailEndTime;

    /**
     * 呼入ivr留言结束 按键dtmf
     */
    private String voiceMailStopDtmf;

    /**
     * 录制文件id
     */
    private String recordId;

    /**
     * 录制文件目录
     */
    private String recordFileName;

    /**
     * 开始ivr时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startIvrTime;

    /**
     * 是否为满意度
     */
    private Boolean satisfaction;

    /**
     * 开始满意度时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startSatisfactionTime;

    /**
     * 是否为转接IVR
     */
    private Boolean transIVR;

    /**
     * 开始转接IVR时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTransIvrTime;

    /**
     * 子话单 AB路对应关系 添加元素
     * <pre>
     * key: {a-uuid}@{b-uuid}
     * value: 是否成功建立通话 true@true
     * </pre>
     */
    public void putCdrLink(String callId, String sourceUUID, String destUUID, Boolean source, Boolean dest) {
        String key = callId + StrUtil.AT + sourceUUID + StrUtil.AT + destUUID;
        String value = source + StrUtil.AT + dest;
        if (CollUtil.isEmpty(this.getCdrLink())) {
            Map<String, String> cdrLink = new LinkedHashMap<>(8);
            cdrLink.put(key, value);
            this.setCdrLink(cdrLink);
            return;
        }
        this.getCdrLink().put(key, value);
    }

    /**
     * 子话单 AB路对应关系 添加元素
     * <pre>
     * key: {a-uuid}@{b-uuid}
     * value: 是否成功建立通话 true@true
     * </pre>
     */
    public void putCdrLink(String sourceUUID, String destUUID, Boolean source, Boolean dest) {
        String mainCallId = this.getMainCallId();
        String key = mainCallId + StrUtil.AT + sourceUUID + StrUtil.AT + destUUID;
        String value = source + StrUtil.AT + dest;
        if (CollUtil.isEmpty(this.getCdrLink())) {
            Map<String, String> cdrLink = new LinkedHashMap<>(8);
            cdrLink.put(key, value);
            this.setCdrLink(cdrLink);
            return;
        }
        this.getCdrLink().put(key, value);
    }

    /**
     * 设置 relationUuid
     *
     * @param uuid 通话id
     */
    public void fillRelationUuidSet(String uuid) {
        if (CollUtil.isEmpty(this.relationUuid)) {
            HashSet<String> set = new HashSet<>();
            set.add(uuid);
            this.setRelationUuid(set);
            return;
        }
        this.getRelationUuid().add(uuid);
    }

    /**
     * 填充three_way的uuid
     */
    public void fillRelateUuidDtoByThreeWay(String uuid) {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            this.setRelateUuidDTO(RelateUuidDTO.builder().threeWayUUID(uuid).build());
            return;
        }
        this.getRelateUuidDTO().setThreeWayUUID(uuid);
    }

    /**
     * 填充consult的uuid
     */
    public void fillRelateUuidDtoByConsult(String uuid) {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            this.setRelateUuidDTO(RelateUuidDTO.builder().consultUUID(uuid).build());
            return;
        }
        this.getRelateUuidDTO().setConsultUUID(uuid);
    }

    /**
     * 填充咨询转的uuid
     */
    public void fillRelateUuidDtoByConsultTrans(String uuid) {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            this.setRelateUuidDTO(RelateUuidDTO.builder().consulTransUUID(uuid).build());
            return;
        }
        this.getRelateUuidDTO().setConsulTransUUID(uuid);
    }

    /**
     * 填充盲转的uuid
     */
    public void fillRelateUuidDtoByBlindTrans(String uuid) {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            this.setRelateUuidDTO(RelateUuidDTO.builder().blindTransUUID(uuid).build());
            return;
        }
        this.getRelateUuidDTO().setBlindTransUUID(uuid);
    }

    /**
     * 填充外呼并桥接的uuid
     */
    public void fillRelateUuidDtoByCallBridge(String uuid) {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            this.setRelateUuidDTO(RelateUuidDTO.builder().bridgeUUID(uuid).build());
            return;
        }
        this.getRelateUuidDTO().setBridgeUUID(uuid);
    }

    /**
     * 获取桥接uuid
     */
    @JsonIgnore
    public String getBridgeUUID() {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            return null;
        }
        return this.getRelateUuidDTO().getBridgeUUID();
    }

    /**
     * 管理员代接uuid
     */
    public void fillRelateUuidDtoBySubstitute(String uuid) {
        if (Objects.isNull(this.getRelateUuidDTO())) {
            this.setRelateUuidDTO(RelateUuidDTO.builder().substituteUUID(uuid).build());
            return;
        }
        this.getRelateUuidDTO().setSubstituteUUID(uuid);
    }

    /**
     * 获取管理员代接uuid
     */
    public String findSubstituteUUID() {
        return this.getRelateUuidDTO().getSubstituteUUID();
    }

    /**
     * 呼入客户uuid
     */
    public String findClientUUID() {
        return this.getRelateUuidDTO().getClientUUID();
    }

    /**
     * 获取主uuid
     */
    @JsonIgnore
    public String getMainUUID() {
        return this.current.getMainUuid();
    }

    /**
     * 获取主通话id
     *
     * @return 主通话id
     */
    @JsonIgnore
    public String getMainCallId() {
        return this.current.getMainCallId();
    }

    @JsonIgnore
    public String getClientUUID() {
        return this.relateUuidDTO.getClientUUID();
    }

    @JsonIgnore
    public void setClientUUID(String clientUUID) {
        RelateUuidDTO relation = this.relateUuidDTO;
        if (Objects.isNull(relation)) {
            RelateUuidDTO dto = new RelateUuidDTO();
            dto.setClientUUID(clientUUID);
            this.relateUuidDTO = dto;
            return;
        }
        relation.setClientUUID(clientUUID);
    }

    /**
     * 设置通话事件的标志和时间
     */
    public void fillCallCdrDTO(CallStatusEventEnum callStatusEvent, Long timestamp) {
        CallCdrDTO callCdrDTO = this.getCurrent().getCallCdrDTO();
        if (Objects.isNull(callCdrDTO)) {
            CallCdrDTO cdrDTO = new CallCdrDTO();
            this.getCurrent().setCallCdrDTO(cdrDTO);
            callCdrDTO = cdrDTO;
        }
        switch (callStatusEvent) {
            case INVITE:
                callCdrDTO.setInviteFlag(true);
                callCdrDTO.setInviteTimestamp(timestamp);
                return;
            case RING:
                callCdrDTO.setRingFlag(true);
                callCdrDTO.setRingTimestamp(timestamp);
                return;
            case ANSWER:
                callCdrDTO.setAnswerFlag(true);
                callCdrDTO.setAnswerTimestamp(timestamp);
                return;
            case BRIDGE:
                callCdrDTO.setBridgeFlag(true);
                callCdrDTO.setBridgeTimestamp(timestamp);
                return;
            case HANGUP:
                callCdrDTO.setHangupFlag(true);
                callCdrDTO.setHangupTimestamp(timestamp);
                return;
            default:
        }
    }

    /**
     * 设置工单id
     *
     * @param workOrderId 工单id
     */
    public void fillWorkOrderId(String workOrderId) {
        if (StrUtil.isEmpty(workOrderId)) {
            return;
        }
        if (Objects.isNull(this.extra)) {
            this.extra = new Extra();
        }
        this.extra.setWorkOrderId(workOrderId);
    }

    /**
     * 获取工单id
     *
     * @return 工单id
     */
    @JsonIgnore
    public String getWorkOrderId() {
        if (Objects.isNull(this.extra)) {
            return null;
        }
        return this.extra.getWorkOrderId();
    }

    @JsonIgnore
    public Integer getAudio() {
        return this.current.getAudio();
    }

    @JsonIgnore
    public Integer getVideo() {
        return this.current.getVideo();
    }

    @JsonIgnore
    public String getCompanyCode() {
        return this.current.getCompanyCode();
    }

    @JsonIgnore
    public String getUUID() {
        return this.current.getUuid();
    }

    @JsonIgnore
    public String getAgentId() {
        return this.current.getAgentId();
    }

    @JsonIgnore
    public String getExtId() {
        return this.current.getExtId();
    }

    @JsonIgnore
    public String getExtIp() {
        return this.current.getExtIp();
    }

    @JsonIgnore
    public String getOs() {
        return this.current.getOs();
    }

    @JsonIgnore
    public String getNumber() {
        return this.current.getNumber();
    }

    @JsonIgnore
    public String getCallerNumber() {
        return this.current.getCallerNumber();
    }

    @JsonIgnore
    public String getCalleeNumber() {
        return this.current.getCalleeNumber();
    }

    @JsonIgnore
    public String getDisplayNumber() {
        return this.current.getDisplayNumber();
    }

    @JsonIgnore
    public CallTypeEnum getCallTypeEnum() {
        return this.current.getCallTypeEnum();
    }

    @JsonIgnore
    public CallRoleEnum getCallRoleEnum() {
        return this.current.getCallRoleEnum();
    }

    @JsonIgnore
    public CallCdrDTO getCallCdrDTO() {
        return this.current.getCallCdrDTO();
    }

    @JsonIgnore
    public CallInChannelEnum getCallInChannel() {
        return this.current.getCallInChannel();
    }

    @JsonIgnore
    public XferActionEnum getXferAction() {
        return this.current.getXferActionEnum();
    }

    @JsonIgnore
    public String getXferUUID() {
        return this.current.getXferUUID();
    }

    @JsonIgnore
    public String findRelationUUID() {
        return this.current.getRelationUuid();
    }

    @JsonIgnore
    public CallDirectionEnum getCallDirection() {
        return this.current.getCallDirectionEnum();
    }

    @JsonIgnore
    public Boolean getChangeMediaFlag() {
        return this.current.getChangeMediaFlag();
    }

    @JsonIgnore
    public String getDa2Result() {
        return this.getCallCdrDTO().getDa2Result();
    }

    @JsonIgnore
    public String getServerId() {
        return this.current.getServerId();
    }

    @JsonIgnore
    public boolean isCheckoutToOffline() {
        return Boolean.TRUE.equals(this.current.getCheckoutToOffline());
    }

    @JsonIgnore
    public String getPlayRecordPath() {
        return this.current.getPlayRecordPath();
    }

    @JsonIgnore
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return this.current.getOriginateAfterActionEnum();
    }

    @JsonIgnore
    public RecordNodeEnum getRecordNode() {
        return this.current.getRecordNode();
    }

    /**
     * 是否未接通
     *
     * @return 是否未接通
     */
    @JsonIgnore
    public Boolean isNoAnswer() {
        CallCdrDTO callCdrDTO = this.current.getCallCdrDTO();
        if (Objects.isNull(callCdrDTO)) {
            return false;
        }
        return !Boolean.TRUE.equals(callCdrDTO.getAnswerFlag());
    }
}
