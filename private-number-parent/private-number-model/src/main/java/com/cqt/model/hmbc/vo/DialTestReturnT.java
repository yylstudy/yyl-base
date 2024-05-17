/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.cqt.model.hmbc.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 拨测服务返回体格式
 *
 * @author scott
 * @date 2022年08月31日 17:02
 */
@Data
public class DialTestReturnT implements Serializable {

    private static final long serialVersionUID = 3186663624244437515L;

    private String callId;
    private String reason;
    private String result;

    public DialTestReturnT() {
    }

    public DialTestReturnT(String callId) {
        this.callId = callId;
        this.result = "0000";
        this.reason = "success";
    }

    public static DialTestReturnT ok(String callId) {
        return new DialTestReturnT(callId);
    }
}
