package com.cqt.sdk.client.event.agentstatus;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:52
 * 离线坐席队列操作事件
 */
@Getter
public class OfflineAgentQueueEvent extends ApplicationEvent {

    private final String companyCode;

    private final String agentId;

    private final String phoneNumber;

    private final AgentServiceModeEnum serviceMode;

    private final Long currentTimestamp;

    private final OperateTypeEnum operateTypeEnum;

    public OfflineAgentQueueEvent(Object source,
                                  String companyCode,
                                  String agentId,
                                  AgentServiceModeEnum serviceMode,
                                  Long currentTimestamp,
                                  OperateTypeEnum operateTypeEnum) {
        super(source);
        this.companyCode = companyCode;
        this.agentId = agentId;
        this.phoneNumber = null;
        this.serviceMode = serviceMode;
        this.currentTimestamp = currentTimestamp;
        this.operateTypeEnum = operateTypeEnum;
    }

    public OfflineAgentQueueEvent(Object source,
                                  String companyCode,
                                  String agentId,
                                  Long currentTimestamp,
                                  OperateTypeEnum operateTypeEnum) {
        super(source);
        this.companyCode = companyCode;
        this.agentId = agentId;
        this.phoneNumber = null;
        this.serviceMode = null;
        this.currentTimestamp = currentTimestamp;
        this.operateTypeEnum = operateTypeEnum;
    }

    public OfflineAgentQueueEvent(Object source,
                                  String companyCode,
                                  String agentId,
                                  String phoneNumber,
                                  Long currentTimestamp,
                                  OperateTypeEnum operateTypeEnum) {
        super(source);
        this.companyCode = companyCode;
        this.agentId = agentId;
        this.phoneNumber = phoneNumber;
        this.serviceMode = null;
        this.currentTimestamp = currentTimestamp;
        this.operateTypeEnum = operateTypeEnum;
    }
}
