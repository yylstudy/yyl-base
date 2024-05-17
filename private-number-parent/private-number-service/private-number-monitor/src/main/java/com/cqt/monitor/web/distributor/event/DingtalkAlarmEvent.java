package com.cqt.monitor.web.distributor.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * @since 2022-12-05 14:22
 * 钉钉告警事件
 */
public class DingtalkAlarmEvent extends ApplicationEvent {

    @Getter
    private String message;

    public DingtalkAlarmEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
}
