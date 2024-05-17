package com.cqt.monitor.web.distributor.event;

import com.cqt.common.enums.OperateTypeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * @since 2022-12-06 9:20
 * 异常节点记录redis
 */
public class RecordFailNodeEvent extends ApplicationEvent {

    @Getter
    private OperateTypeEnum operateTypeEnum;

    public RecordFailNodeEvent(Object source, OperateTypeEnum operateTypeEnum) {
        super(source);
        this.operateTypeEnum = operateTypeEnum;
    }

}
