package com.cqt.broadnet.common.model.axb.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-05-25 15:33:15:33
 */
@Data
public class BroadNetAxbBindVO implements Serializable {

    private static final long serialVersionUID = 2629828495186463525L;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 号码X
     */
    private String telX;

    /**
     * 属性, 返回中间号的属性
     */
    private String flag;
}
