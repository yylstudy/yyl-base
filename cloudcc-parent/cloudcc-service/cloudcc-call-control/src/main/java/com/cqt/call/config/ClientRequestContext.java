package com.cqt.call.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cqt.model.client.base.ClientRequestBaseDTO;

/**
 * @author linshiqiang
 * date:  2023-07-13 9:35
 * 前端sdk请求 的公共参数上下文
 */
public class ClientRequestContext {

    private static final TransmittableThreadLocal<ClientRequestBaseDTO> CONTEXT = new TransmittableThreadLocal<>();

    public static void set(ClientRequestBaseDTO suffix) {
        CONTEXT.set(suffix);
    }

    public static ClientRequestBaseDTO get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }
}
