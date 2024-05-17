package com.cqt.unicom.vo;

import lombok.Data;

/**
 * @author huweizhong
 * date  2023/7/5 19:38
 *
 *
 */
@Data
public class ResultErrVO {

    private Integer code;

    private String msg;

    public static ResultErrVO fail(String msg){
        ResultErrVO resultErrVO = new ResultErrVO();
        resultErrVO.setCode(99);
        resultErrVO.setMsg(msg);
        return resultErrVO;
    }

    public static ResultErrVO ok(){
        ResultErrVO resultErrVO = new ResultErrVO();
        resultErrVO.setCode(200);
        resultErrVO.setMsg("success");
        return resultErrVO;
    }
}
