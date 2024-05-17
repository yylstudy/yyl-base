package com.cqt.model.unicom.entity;


import lombok.Data;

/**
 * 调用COMMCdrPUSH返回体
 *
 * @author zhengsuhao
 * @date 2022/12/9
 */
@Data
public class CommResult {

    private String result;

    private String reason;
    
    private String callId;


}
