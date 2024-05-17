package com.cqt.base.util;

import cn.hutool.core.text.StrFormatter;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcServiceContext;

/**
 * @author linshiqiang
 * date:  2023-08-09 10:11
 */
public class RpcContextUtil {

    private static final String TRACE_ID = "requestId";

    /**
     * 构建traceId
     */
    public static String buildTraceId(String reqId, String msgType, String companyCode, String agentId, String extId) {
        String traceIdFormat = "{}@{}@{}@{}@{}";
        return StrFormatter.format(traceIdFormat, reqId,
                msgType,
                companyCode,
                agentId,
                extId);
    }

    /**
     * 设置 TRACE_ID
     */
    public static void set(String reqId, String msgType, String companyCode, String agentId, String extId) {
        RpcServiceContext serviceContext = RpcContext.getServiceContext();
        String traceId = buildTraceId(reqId, msgType, companyCode, agentId, extId);
        serviceContext.setAttachment(TRACE_ID, traceId);
    }

    /**
     * 设置 TRACE_ID
     */
    public static void set(String traceId) {
        RpcServiceContext serviceContext = RpcContext.getServiceContext();
        serviceContext.setAttachment(TRACE_ID, traceId);
    }

    /**
     * 获取 TRACE_ID
     */
    public static String getTraceId() {
        RpcServiceContext serviceContext = RpcContext.getServiceContext();
        return serviceContext.getAttachment(TRACE_ID);
    }

    public static void clear() {
        RpcContext.getServiceContext().clearAttachments();
    }
}
