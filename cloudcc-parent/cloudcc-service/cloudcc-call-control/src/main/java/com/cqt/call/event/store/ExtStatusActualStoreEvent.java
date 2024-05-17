package com.cqt.call.event.store;

import com.cqt.model.cdr.entity.ExtStatusLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:34
 * 坐席状态迁移日志事件 发送mq进行入库
 * 保存redis
 */
@Getter
public class ExtStatusActualStoreEvent extends ApplicationEvent {

    private final ExtStatusLog extStatusLog;

    public ExtStatusActualStoreEvent(Object source, ExtStatusLog extStatusLog) {
        super(source);
        this.extStatusLog = extStatusLog;
    }

}
