package com.cqt.common.constants;

/**
 * @author linshiqiang
 * @date 2021/7/16 16:50
 */
public interface SystemConstant {

    /**
     * 负号 异地
     */
    String MINUS = "-";

    /**
     * 加号 本地
     */
    String PLUS = "+";

    /**
     * 等号
     */
    String EQUAL_SIGN = "=";

    /**
     * 全国池
     */
    String COUNTRY_CODE = "0000";

    /**
     * 默认企业 - 0000
     */
    String DEFAULT_VCC_ID = "0000";

    String ONE = "1";

    String ZERO = "0";

    Integer NUMBER_ONE = 1;

    Integer NUMBER_THREE = 3;

    String A = "A";

    String B = "B";

    String X = "X";

    String AXB = "cqt-axb";

    String AXE = "cqt-axe";

    String AYB = "cqt-ayb";

    String AXYB = "cqt-axyb";

    String AXEYB = "cqt-axeyb";

    String AXEBN = "cqt-axebn";

    String AX = "cqt-ax";

    /**
     * bindId前缀
     */
    String BIND_ID_SUFFIX = "cqt-";

    /**
     * 空对象
     */
    String EMPTY_OBJECT = "{}";

    String URL_PATTERNS = "/api/v1/bind/**";

    String BIND_URI = "/api/v1/bind/";

    String THIRD_CDR_URI = "/api/v2/bind/";

    /**
     * 业务redis location
     * 配置对应cluster3
     */
    String BIZ_REDIS = "biz";

    String REMOTE_IP = "remoteIp";
    String REQUEST_ID = "requestId";

    String STEP_ID = "stepId";

    String MID_SERVICE_KEY = "900007";

    String SIGN = "sign";

    String TS = "ts";
}
