package com.cqt.common.constants;

/**
 * @author linshiqiang
 * @date 2021/9/16 17:07
 */
public interface GatewayConstant {

    String CACHED_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    String CACHED_REQUEST_PATH_KEY = "cachedRequestPath";

    String CACHED_FORWARD_URL_KEY = "cachedForwardUrl";

    String CACHED_EXCEPTION_KEY = "cachedException";

    String CACHED_AREA_CODE_KEY = "cachedAreaCode";

    String CACHED_REQUEST_ID_KEY = "cachedRequestId";

    String CACHED_SUPPLIER_ID_KEY = "cachedSupplierId";

    String CACHED_BIND_ID_KEY = "cachedBindId";

    String CACHED_BREAK_FLAG_KEY = "cachedBreakFlag";

    String WHOLE_AREA = "wholearea";

    String AREA_CODE = "area_code";

    String BIND_ID = "bind_id";

    String REQUEST_ID = "request_id";

    String SUPPLIER_ID = "supplier_id";

    String CALLED = "called";

    /**
     * 本地供应商
     */
    String LOCAL = "local";

    String HDH = "hdh";

    String BACK = "back";

    String A = "A";

    String B = "B";

    String BIND_INFO_API = "/api/v1/bind/query";

    /**
     * 设置绑定接口url
     */
    String BINDING = "binding";

    String UNKNOWN = "unknown";
}
