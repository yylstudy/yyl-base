package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:37
 * 通过接口调用FS往通话写入通道变量
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SetSessionVarDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -2880984589436651702L;

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 是  | 写入通道变量键
     */
    private String key;

    /**
     * 是  | 写入通道变量值
     */
    private String value;
}
