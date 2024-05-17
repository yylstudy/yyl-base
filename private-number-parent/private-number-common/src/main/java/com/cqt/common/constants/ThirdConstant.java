package com.cqt.common.constants;


/**
 * @author dingsh
 * @date 2022/7/27
 */
public interface ThirdConstant {
    /**
     * 用户呼叫X 时, 被叫接到呼叫时的主显号码。
     * 2：显示X 号码(默认值)
     * 3：显示真实号码
     */
    Integer MODEL_X = 2;

    Integer MODEL_AB = 3;

    String HDH_SUCCESS_CODE = "0000";

    Integer HTTP_STATUS_SUCCESS_CODE = 200;

    String warnMessage = "【和多号平台接口告警】\n" +
            "告警时间: %s\n" +
            "当前设备: %s\n" +
            "告警内容: %s\n" +
            "接口调用异常=>\n" +
            "ConnectException: %s\n" +
            "@所有人\u0007";

    String WARN_TYPE = "dingding";

    String OPERATE_TYPE = "调用第三方接口";


    /**
     号码类型常量
     */
    String  NUMBER_FIXED="1";

    String  NUMBER_400="2";

    String  NUMBER_95="3";

    String  NUMBER_XH="4";


    /**
     * 号码匹配规则
     **/
    String  REGEX_95="^95[0-9]+";

    String  REGEX_400="^400[0-9]+";

    String  REGEX_FIXED= "^0[1-9][0-9]+";

    String  REGEX_XH="^0[1-9][0-9]+";

    /**
     *  第三方 urlType
     **/
    String URL_TYPE_BIND="1";

    String URL_TYPE_UNBIND="2";

    String URL_TYPE_UPDATE_EXPIRATION_BIND="3";

    /**
     *  和多号（token）
     **/
    String AUTHORIZATION="EOPAUTH platformid=\"%s\",timestamp=\"%s\",signature=\"%s\"";


    /**
     *  (通话话单)交换机名称
     **/

    String ICCPCDRSAVEEXCHANGE="iccp_cdr_save_exchange";

    /**
     *  (通话话单)路由键
     **/
    String ICCPCDRSAVEROUTEKEY="iccp_cdr_save_routing";

    /**
     *  (短信话单)交换机名称
     **/
    String ICCPSMSCDRSAVEEXCHANGE = "iccp_sms_sdr_exchange";

    /**
     *  (短信话单)路由键
     **/
    String ICCPSMSCDRSAVEROUTEKEY = "iccp_sms_sdr_routing";

    /**
     *  推送
     **/
    String PUSH_FAIL_CODE="1001";

    /**
     *  日期格式
     **/
    String yyyyMMddHHmmss="yyyyMMddHHmmss";

    String yyyy_MM_dd_HH_mm_ss="yyyy-MM-dd HH:mm:ss";
}