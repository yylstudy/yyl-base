package com.cqt.cloudcc.manager.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cqt.model.agent.entity.AgentInfo;

/**
 * @author linshiqiang
 * date:  2023-11-07 19:19
 */
public class AgentInfoContext {

    private static final TransmittableThreadLocal<AgentInfo> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(AgentInfo agentInfo) {
        THREAD_LOCAL.set(agentInfo);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
    
    public static AgentInfo get() {
        return THREAD_LOCAL.get();
    }
}
