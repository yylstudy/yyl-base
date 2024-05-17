package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-08-14 15:54
 * <pre>
 * ret_success: 0,   //成功返回值
 * param_error: -1,   //参数请求错误
 * api_error_option: -2,  //配置错误 api查询数据库，redis返回的错误
 * api_error_extstatus:-3, //分机状态异常
 * acd_error:-4,      //api调用acd接口返回的错误
 * sbc_error:-5,      //api调用sbc接口返回的错误
 * api_error_uuid:-6,   //redis中无法查到uuid的错误
 * unkown:-100       //未知错误
 * </pre>
 */
public enum FreeswitchResultCode {

    SUCCESS(0, "成功"),

    API_REQUEST_ERROR(-10000, "底层接口调用异常"),

    PARAM_ERROR(-1, "参数请求错误"),

    API_ERROR_OPTION(-2, "配置错误 api查询数据库，redis返回的错误"),

    API_ERROR_EXT_STATUS(-3, "分机状态异常"),

    ACD_ERROR(-4, "api调用acd接口返回的错误"),

    SBC_ERROR(-5, "api调用sbc接口返回的错误"),

    API_ERROR_UUID(-6, "redis中无法查到uuid的错误"),

    UNKNOWN(-100, "未知错误");

    private final Integer code;

    private final String name;

    FreeswitchResultCode(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
