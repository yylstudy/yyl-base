package com.cqt.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @ClassName ResponseData
 * @author: linshiqiang 656667021@qq.com
 * @Date: 2019-12-11 11:26
 * @Description:
 */
@Data
@NoArgsConstructor
public class ResponseData implements Serializable {

    private static final long serialVersionUID = -2413208356148137534L;

    private String msg;

    private Object data;

    private Integer code;

    private Boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long recordsTotal;

    private final static String SUCCESS_MSG = "操作成功";
    private final static String FAIL_MSG = "操作失败";

    private final static Integer SUCCESS_CODE = 200;
    private final static Integer ERROR_CODE = 400;

    private final static String KEY_DATA = "data";
    private final static String KEY_TOTAL = "recordsTotal";

    public ResponseData(String msg, Integer code, Boolean success, Object data) {
        this.msg = msg;
        this.data = data;
        this.code = code;
        this.success = success;
    }

    public ResponseData(String msg, Integer code, Boolean success) {
        this.msg = msg;
        this.code = code;
        this.success = success;
    }

    public ResponseData(String msg, Object data, Integer code, Boolean success, Long recordsTotal) {
        this.msg = msg;
        this.data = data;
        this.code = code;
        this.success = success;
        this.recordsTotal = recordsTotal;
    }

    public static ResponseData successWithPageList(Object data, Long recordsTotal) {
        Map<String, Object> finalData = new HashMap<>(16);
        finalData.put(KEY_DATA, data);
        finalData.put(KEY_TOTAL, recordsTotal);
        return new ResponseData(SUCCESS_MSG, SUCCESS_CODE, true, finalData);
    }

    public static ResponseData successWithDataTotal(Object data, Long recordsTotal) {
        return new ResponseData(SUCCESS_MSG, data, SUCCESS_CODE, true, recordsTotal);
    }

    public static ResponseData successWithData(Object data) {
        return new ResponseData(SUCCESS_MSG, SUCCESS_CODE, true, data);
    }

    public static ResponseData successWithData(Object data, String msg) {
        return new ResponseData(msg, SUCCESS_CODE, true, data);
    }

    public static ResponseData successWithData(Object data, String msg, Integer code) {
        return new ResponseData(msg, code, true, data);
    }

    public static ResponseData success() {
        return new ResponseData(SUCCESS_MSG, SUCCESS_CODE, true);
    }

    public static ResponseData success(String msg) {
        return new ResponseData(msg, SUCCESS_CODE, true);
    }

    public static ResponseData fail(String errMsg, Integer errCode) {
        return new ResponseData(errMsg, errCode, false);
    }

    public static ResponseData fail(String errMsg) {
        return new ResponseData(errMsg, ERROR_CODE, false);
    }

    public static ResponseData fail() {
        return new ResponseData(FAIL_MSG, ERROR_CODE, false);
    }

}
