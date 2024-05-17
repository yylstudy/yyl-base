package com.cqt.model.unicom.vo;

import io.swagger.annotations.Api;
import lombok.Data;

/**
 * @author zhengsuhao
 * @date 2022/12/6
 */
@Api(tags="联通集团总部(江苏)话单通用出参报文")
@Data
public class GeneralMessageVO {

    private Boolean success;

    private String message;

    private String code;

    private Object data;

    public GeneralMessageVO() {
    }

    public static GeneralMessageVO ok() {
        GeneralMessageVO generalMessageVO = new GeneralMessageVO();
        generalMessageVO.success = true;
        generalMessageVO.message = "success";
        generalMessageVO.code = "C0000000";
        generalMessageVO.data = "通话状态报文推送成功";
        return generalMessageVO;
    }

    public static GeneralMessageVO ok(String message) {
        GeneralMessageVO generalMessageVO = new GeneralMessageVO();
        generalMessageVO.success = true;
        generalMessageVO.message = message;
        generalMessageVO.code = "C0000000";
        generalMessageVO.data = "通话状态报文推送成功";
        return generalMessageVO;
    }

    public static GeneralMessageVO ok(String message, Object data) {
        GeneralMessageVO generalMessageVO = new GeneralMessageVO();
        generalMessageVO.success = true;
        generalMessageVO.message = message;
        generalMessageVO.code = "C0000000";
        generalMessageVO.data = data;
        return generalMessageVO;
    }

    public static GeneralMessageVO fail(String code, Object data) {
        GeneralMessageVO generalMessageVO = new GeneralMessageVO();
        generalMessageVO.success = false;
        generalMessageVO.message = "失败";
        generalMessageVO.code = "C0009999";
        generalMessageVO.data = data;
        return generalMessageVO;
    }

}
