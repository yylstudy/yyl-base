package com.cqt.monitor.web.distributor.event;

import com.cqt.monitor.web.distributor.model.dto.UpdateDistributorDbDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * @since 2022-12-05 9:45
 * dis组更新db事件
 */
public class UpdateDistributorDbEvent extends ApplicationEvent {

    @Getter
    private UpdateDistributorDbDTO updateDistributorDbDTO;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public UpdateDistributorDbEvent(Object source) {
        super(source);
    }

    public UpdateDistributorDbEvent(Object source, UpdateDistributorDbDTO updateDistributorDbDTO) {
        super(source);
        this.updateDistributorDbDTO = updateDistributorDbDTO;
    }
    
}
