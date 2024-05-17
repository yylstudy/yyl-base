package com.cqt.model.corpinfo.entity;

import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-03-27 16:07
 */
@Data
public class PrivateVccCurManage {

    private String vccId;


    private String vccName;

    /**
     * 是否开启企业呼叫并发管控
     */
    private String isVcc;


    /**
     * 企业呼叫并发管控数
     */
    private Integer vccNum;

}
