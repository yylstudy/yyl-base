package com.cqt.queue.calltask.aspect;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cqt.model.calltask.dto.CallTaskOperateDTO;

/**
 * @author linshiqiang
 * date:  2023-11-13 16:04
 * 任务信息上下文
 */
public class TaskInfoContext {

    private static final ThreadLocal<CallTaskOperateDTO> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(CallTaskOperateDTO callTaskOperateDTO) {
        THREAD_LOCAL.set(callTaskOperateDTO);
    }

    public static CallTaskOperateDTO get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
