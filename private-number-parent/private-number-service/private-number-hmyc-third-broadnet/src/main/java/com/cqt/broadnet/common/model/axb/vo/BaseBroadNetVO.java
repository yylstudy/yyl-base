package com.cqt.broadnet.common.model.axb.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-05-25 15:46:15:46
 */
@Data
public class BaseBroadNetVO<T> implements Serializable {

    private static final long serialVersionUID = -9132095174414984554L;

    /**
     * 返回码
     */
    private Integer code;


    /**
     * 状态码, 系统状态码固定为0
     */
    private Integer state;

    /**
     * 错误描述信息
     */
    private String message;

    /**
     * 接口返回体
     */
    private T data;
}
