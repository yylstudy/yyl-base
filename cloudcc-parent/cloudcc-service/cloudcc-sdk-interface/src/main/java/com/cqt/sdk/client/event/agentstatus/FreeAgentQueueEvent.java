package com.cqt.sdk.client.event.agentstatus;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:52
 * 空闲坐席队列操作事件
 */
@Getter
public class FreeAgentQueueEvent extends ApplicationEvent {

    private final String companyCode;

    private final String agentId;

    private final AgentServiceModeEnum serviceMode;

    private final Long currentTimestamp;

    private final OperateTypeEnum operateTypeEnum;

    public FreeAgentQueueEvent(Object source,
                               String companyCode,
                               String agentId,
                               AgentServiceModeEnum serviceMode,
                               Long currentTimestamp,
                               OperateTypeEnum operateTypeEnum) {
        super(source);
        this.companyCode = companyCode;
        this.agentId = agentId;
        this.serviceMode = serviceMode;
        this.currentTimestamp = currentTimestamp;
        this.operateTypeEnum = operateTypeEnum;
    }

    public FreeAgentQueueEvent(Object source,
                               String companyCode,
                               String agentId,
                               Long currentTimestamp,
                               OperateTypeEnum operateTypeEnum) {
        super(source);
        this.companyCode = companyCode;
        this.agentId = agentId;
        this.serviceMode = null;
        this.currentTimestamp = currentTimestamp;
        this.operateTypeEnum = operateTypeEnum;
    }
}
