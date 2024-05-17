package com.cqt.model.freeswitch.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-08-01 17:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetSessionVarVO extends FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = -7039796063485524528L;

    /**
     * 通道变量值
     */
    private String value;
}
