package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:38
 * 通过接口调用FS获取session通道变量
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetSessionVarDTO extends FreeswitchApiBase implements Serializable {

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 是  | 要获取通道变量键
     */
    private String key;

}
