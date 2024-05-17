package com.cqt.recycle.web.corpinfo.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * @date 2022/7/27 14:11
 * 业务配置同步事件
 */
@Getter
public class BusinessEvent extends ApplicationEvent {

    private String vccId;

    private String businessType;

    public BusinessEvent(Object source, String vccId, String businessType) {
        super(source);
        this.vccId = vccId;
        this.businessType = businessType;
    }

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public BusinessEvent(Object source) {
        super(source);
    }
}
