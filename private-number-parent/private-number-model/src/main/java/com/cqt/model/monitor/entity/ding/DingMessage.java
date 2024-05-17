package com.cqt.model.monitor.entity.ding;

import lombok.Data;

/**
 * @date 2021-09-27
 * @author hlx
 */
@Data
public class DingMessage {

    /**
     * 消息类型  固定text
     */
    private String msgtype;

    private DingText text;

    private DingAt at;
}
