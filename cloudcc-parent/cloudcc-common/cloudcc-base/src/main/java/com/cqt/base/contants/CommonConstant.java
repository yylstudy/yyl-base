package com.cqt.base.contants;

/**
 * @author Xienx
 * date 2023-07-28 15:27:15:27
 */
public interface CommonConstant {

    /**
     * 全局企业code 000000
     */
    String GLOBAL_COMPANY_CODE = "000000";

    String COMPANY_CODE_KEY = "company_code";

    String MONTH_KEY = "month";

    String VCC_ID_KEY = "vcc_id";

    String MONTH_FORMAT = "yyyyMM";

    String DATE_FORMAT = "yyyyMMdd";

    /**
     * 通用是否 - 0 - 否
     */
    Integer ENABLE_N = 0;

    /**
     * 通用是否 - 1 - 是
     */
    Integer ENABLE_Y = 1;

    /**
     * 分机注册方式 1 - webrtc
     * */
    Integer EXT_REG_MODE_WEBRTC = 1;

    /**
     * 分机注册方式 2 - 其他第三方话机
     * */
    Integer EXT_REG_MODE_OTHER = 2;
}
